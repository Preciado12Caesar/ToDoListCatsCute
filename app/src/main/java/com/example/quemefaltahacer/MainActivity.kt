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
import androidx.compose.material.icons.filled.Info
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
import android.content.Intent
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.EaseOutBounce
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures

class MainActivity : ComponentActivity() {
    private lateinit var repository: TaskRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = TaskDatabase.getDatabase(this)
        repository = TaskRepository(database.taskDao())
        startService(Intent(this, MusicService::class.java))

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
    var showAuthorSheet by remember { mutableStateOf(false) }
    val pendingTasks by repository.getPendingTasks().collectAsState(initial = emptyList())
    val completedTasks by repository.getCompletedTasks().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    // BOX PRINCIPAL CON PADDING PARA PANTALLAS CON NOTCH/CÁMARA GOTA
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            // SOLUCIÓN: WindowInsets para evitar el notch/cámara gota
            .windowInsetsPadding(WindowInsets.systemBars),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // === HEADER SECTION (20% de la pantalla - MÁS ESPACIO) ===
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.20f), // Aumentado de 0.15f a 0.20f
                contentAlignment = Alignment.TopCenter
            ) {
                // InfoButton en la esquina superior derecha
                InfoButton(
                    onClick = { showAuthorSheet = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 8.dp) // Más padding superior
                )

                // Título centrado con nube MÁS GRANDE
                TituloConNubeOptimizado(
                    fontSize = 32, // Aumentado de 28 a 32
                    onPlusClick = { showBottomSheet = true },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 24.dp) // Más separación del borde superior
                )
            }

            // === CONTENT SECTION (55% de la pantalla - REDUCIDO) ===
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.55f), // Reducido de 0.65f a 0.55f
                contentAlignment = Alignment.Center
            ) {
                TaskListOptimizada(
                    tasks = pendingTasks,
                    onTaskComplete = { task ->
                        coroutineScope.launch {
                            repository.toggleTaskCompletion(task)
                        }
                    }
                )
            }

            // === BOTTOM SECTION (25% de la pantalla - MÁS ESPACIO PARA GATOS) ===
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.25f), // Aumentado de 0.20f a 0.25f
                contentAlignment = Alignment.BottomCenter
            ) {
                BottomSectionOptimizada(
                    onHistoryClick = { showHistorySheet = true }
                )
            }
        }
    }

    // Modales
    AddTaskBottomSheet(
        isVisible = showBottomSheet,
        onDismiss = { showBottomSheet = false },
        onSaveTask = { description ->
            coroutineScope.launch {
                repository.insertTask(description)
            }
        }
    )

    HistoryBottomSheet(
        isVisible = showHistorySheet,
        onDismiss = { showHistorySheet = false },
        completedTasks = completedTasks
    )

    AuthorBottomSheet(
        isVisible = showAuthorSheet,
        onDismiss = { showAuthorSheet = false }
    )
}

// === BOTÓN INFO OPTIMIZADO ===
@Composable
fun InfoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animación continua de brillo
    val infiniteTransition = rememberInfiniteTransition(label = "shine")
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(56.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2196F3).copy(alpha = shimmer),
                        Color(0xFF1976D2).copy(alpha = 0.9f),
                        Color(0xFF0D47A1)
                    ),
                    radius = 80f
                ),
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Información del autor",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}

// === TÍTULO OPTIMIZADO CON NUBE MÁS GRANDE ===
@Composable
fun TituloConNubeOptimizado(
    fontSize: Int,
    onPlusClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Botón + más grande
        IconButton(
            onClick = onPlusClick,
            modifier = Modifier
                .size(70.dp) // Aumentado de 60dp a 70dp
                .padding(2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar",
                tint = Color(0xFF0099CC),
                modifier = Modifier.size(50.dp) // Aumentado de 40dp a 50dp
            )
        }

        // Nube con texto MÁS GRANDE
        Box(
            modifier = Modifier.size(200.dp) // Aumentado de 160dp a 200dp
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

// === LISTA DE TAREAS OPTIMIZADA ===
@Composable
fun TaskListOptimizada(
    tasks: List<TaskEntity>,
    onTaskComplete: (TaskEntity) -> Unit
) {
    if (tasks.isEmpty()) {
        // Estado vacío mejorado
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "¡No hay tareas pendientes!",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF666666)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Presiona + para agregar una nueva tarea",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color(0xFF999999)
                )
            )
        }
    } else {
        // Lista de tareas con scroll suave
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(tasks) { task ->
                TaskItemOptimizada(
                    task = task,
                    onTaskComplete = onTaskComplete
                )
            }
        }
    }
}

// === ITEM DE TAREA OPTIMIZADO ===
@Composable
fun TaskItemOptimizada(
    task: TaskEntity,
    onTaskComplete: (TaskEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador visual
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color(0xFF0099CC), CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Texto de la tarea
            Text(
                text = task.description,
                modifier = Modifier.weight(1f),
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color(0xFF333333),
                    lineHeight = 24.sp
                )
            )

            // Botón de completar mejorado
            IconButton(
                onClick = { onTaskComplete(task) },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color(0xFF0099CC).copy(alpha = 0.1f),
                        CircleShape
                    )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ico),
                    contentDescription = "Completar tarea",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// === SECCIÓN INFERIOR OPTIMIZADA CON GATOS MÁS GRANDES Y VISIBLES ===
@Composable
fun BottomSectionOptimizada(
    onHistoryClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sway")

    val swayAnimation by infiniteTransition.animateFloat(
        initialValue = -3f, // Más movimiento
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 4000,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp), // Más padding inferior
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // GATOS Y BOTÓN EN LA MISMA FILA
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(x = swayAnimation.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gato 2 - Izquierda
            Image(
                painter = painterResource(id = R.drawable.gato2),
                contentDescription = "Gato 2",
                modifier = Modifier.size(100.dp)
            )

            // Botón de historial en el centro
            Card(
                modifier = Modifier
                    .size(140.dp)
                    .clickable { onHistoryClick() },
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.fondo),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )

                    Text(
                        text = "¿Qué hice hoy?",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0099CC)
                        )
                    )
                }
            }

            // Gato 3 - Derecha
            Image(
                painter = painterResource(id = R.drawable.gato3),
                contentDescription = "Gato 3",
                modifier = Modifier.size(100.dp)
            )
        }
    }
}

// === MODAL DEL AUTOR ===
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit
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
            modifier = Modifier.fillMaxHeight(0.6f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Acerca del Desarrollador",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0099CC)
                    ),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.author),
                        contentDescription = "Foto del autor",
                        modifier = Modifier
                            .size(120.dp)
                            .background(Color.Gray.copy(0.1f), CircleShape)
                    )

                    Column {
                        Text(
                            text = "Autor:",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0099CC)
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "César Jesus Dose Preciado Calderon",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// === MODAL HISTORIAL ===
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

// === MODAL AGREGAR TAREA ===
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

// === PREVIEW ===
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    QueMeFaltaHacerTheme {
        // Preview básico sin repository
        Column {
            TituloConNubeOptimizado(fontSize = 32, onPlusClick = {})
            BottomSectionOptimizada(onHistoryClick = {})
        }
    }
}