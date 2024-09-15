@file:OptIn(ExperimentalMaterial3Api::class)

package br.edu.satc.todolistcompose.ui.screens

import AppDatabase
import TaskData
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import br.edu.satc.todolistcompose.ui.components.TaskCard
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(database: AppDatabase) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = { Text(text = "ToDoList UniSATC") },
                actions = {
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            Icons.Rounded.Settings,
                            contentDescription = null
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Nova tarefa") },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                onClick = {
                    showBottomSheet = true
                }
            )
        }
    ) { innerPadding ->
        // Passa a instância do banco de dados para o HomeContent
        HomeContent(innerPadding = innerPadding, database = database)
        NewTask(showBottomSheet = showBottomSheet) { showBottomSheet = false }
    }
}

@Composable
fun HomeContent(innerPadding: PaddingValues, database: AppDatabase) {
    val taskDao = database.taskDao()
    var tasks by remember { mutableStateOf(emptyList<TaskData>()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            tasks = taskDao.getAllTasks()
        }
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .padding(top = innerPadding.calculateTopPadding())
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            ),
        verticalArrangement = Arrangement.Top
    ) {
        for (task in tasks) {
            TaskCard(
                task = task,
                onTaskCompleteChange = { updatedTask ->
                    coroutineScope.launch {
                        taskDao.updateTask(updatedTask)
                    }
                }
            )
        }
    }
}

@Composable
fun NewTask(showBottomSheet: Boolean, onComplete: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var taskTitle by remember {
        mutableStateOf("")
    }
    var taskDescription by remember {
        mutableStateOf("")
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                onComplete()
            },
            sheetState = sheetState,

            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = {taskTitle = it},
                    label = { Text(text = "Título da tarefa") })
                OutlinedTextField(
                    value = taskDescription,
                    onValueChange = {taskDescription = it},
                    label = { Text(text = "Descrição da tarefa") })
                Button(modifier = Modifier.padding(top = 4.dp), onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            onComplete()
                        }
                    }
                }) {
                    Text("Salvar")
                }
            }
        }
    }
}