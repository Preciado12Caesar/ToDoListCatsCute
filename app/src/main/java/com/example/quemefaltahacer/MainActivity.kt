package com.example.quemefaltahacer

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import kotlinx.coroutines.launch
import com.example.quemefaltahacer.ui.theme.QueMeFaltaHacerTheme
import Data.TaskDatabase
import Data.TaskEntity
import Data.TaskRepository
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.offset

class MainActivity : ComponentActivity() {
    private lateinit var repository: TaskRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = TaskDatabase.getDatabase(this)
        repository = TaskRepository(database.taskDao())

        setContent {
            QueMeFaltaHacerTheme {
                MainScreen(repository)
            }
        }
    }
}

@Composable
fun MainScreen(repository: TaskRepository) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    val pendingTasks by repository.getPendingTasks().collectAsState(initial = emptyList())
    val completedTasks by repository.getCompletedTasks().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            // Título más arriba
            TituloConNube(
                fontSize = 30,
                onPlusClick = { showBottomSheet = true }
            )

            // Lista de tareas (ocupa todo el espacio disponible)
            TaskList(
                tasks = pendingTasks,
                onTaskComplete = { task ->
                    coroutineScope.launch {
                        repository.toggleTaskCompletion(task)
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }

        // Parte inferior fija
        BottomSectionFixed(
            onHistoryClick = { showHistorySheet = true },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Modal para agregar tareas
        AddTaskBottomSheet(
            isVisible = showBottomSheet,
            onDismiss = { showBottomSheet = false },
            onSaveTask = { description ->
                coroutineScope.launch {
                    repository.insertTask(description)
                }
            }
        )

        // Modal para historial
        HistoryBottomSheet(
            isVisible = showHistorySheet,
            onDismiss = { showHistorySheet = false },
            completedTasks = completedTasks
        )
    }
}

@Composable
fun BottomSectionFixed(
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sway")

    // Animación más sutil
    val swayAnimation by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3000,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Botón más pequeño
        // Nube más pequeña clickeable como botón
        Box(
            modifier = Modifier
                .size(180.dp)
                .clickable { onHistoryClick() } // Al tocar la nube, se llama la función
        ) {
            Image(
                painter = painterResource(id = R.drawable.fondo),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )

            Text(
                text = "¿Qué hice hoy?",
                modifier = Modifier.align(Alignment.Center),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0099CC)
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Imágenes más grandes con animación sutil
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .offset(x = swayAnimation.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            Image(
                painter = painterResource(id = R.drawable.gato1),
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )

            Image(
                painter = painterResource(id = R.drawable.gato2),
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )

            Image(
                painter = painterResource(id = R.drawable.gato3),
                contentDescription = null,
                modifier = Modifier.size(105.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    completedTasks: List<TaskEntity>
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = bottomSheetState,
            containerColor = Color.White,
            contentColor = Color.Black,
            modifier = Modifier.fillMaxHeight(0.8f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tareas Completadas",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0099CC)
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (completedTasks.isEmpty()) {
                    Text(
                        text = "No has completado ninguna tarea aún",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = Color.Gray
                        ),
                        modifier = Modifier.padding(32.dp)
                    )
                } else {
                    LazyColumn {
                        items(completedTasks) { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF0F8FF), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ico),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = task.description,
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun TaskList(
    tasks: List<TaskEntity>,
    onTaskComplete: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tasks) { task ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color.Black, CircleShape)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = task.description,
                    modifier = Modifier.weight(1f),
                    fontSize = 16.sp
                )

                IconButton(
                    onClick = { onTaskComplete(task) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ico),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onSaveTask: (String) -> Unit
) {
    var taskDescription by remember { mutableStateOf("") }
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                taskDescription = ""
                onDismiss()
            },
            sheetState = bottomSheetState,
            containerColor = Color.White,
            contentColor = Color.Black,
            modifier = Modifier.fillMaxHeight(0.7f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Nueva Tarea",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0099CC)
                    ),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value = taskDescription,
                    onValueChange = { taskDescription = it },
                    label = { Text("Descripción de la tarea") },
                    placeholder = { Text("Escribe aquí tu tarea...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0099CC),
                        focusedLabelColor = Color(0xFF0099CC),
                        cursorColor = Color(0xFF0099CC)
                    ),
                    singleLine = false,
                    maxLines = 3
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            taskDescription = ""
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF0099CC)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF0099CC))
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            if (taskDescription.trim().isNotEmpty()) {
                                onSaveTask(taskDescription.trim())
                                taskDescription = ""
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0099CC),
                            contentColor = Color.White
                        ),
                        enabled = taskDescription.trim().isNotEmpty()
                    ) {
                        Text("Guardar")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun TituloConNube(fontSize: Int, onPlusClick: () -> Unit) {
    val imageSize = 220.dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Botón + más arriba
        IconButton(
            onClick = onPlusClick,
            modifier = Modifier
                .size(80.dp)
                .padding(top = 10.dp, bottom = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar",
                tint = Color(0xFF0099CC),
                modifier = Modifier.size(52.dp)
            )
        }

        // Nube con texto
        Box(
            modifier = Modifier.size(imageSize)
        ) {
            Image(
                painter = painterResource(id = R.drawable.fondo),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )

            Text(
                text = "CHECKLIST",
                modifier = Modifier.align(Alignment.Center),
                style = TextStyle(
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0099CC)
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TituloConNubePreview() {
    QueMeFaltaHacerTheme {
        Column {
            TituloConNube(fontSize = 30, onPlusClick = {})
        }
    }
}