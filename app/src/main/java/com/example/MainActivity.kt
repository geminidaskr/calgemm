package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.PrecalcViewModel

enum class AppDestination(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    CALCULATOR("Funciones", Icons.Outlined.Calculate),
    STATISTICS("Estadística", Icons.Outlined.Analytics),
    CAMERA_MATH("Cámara IA", Icons.Outlined.PhotoCamera),
    SOLVER("Ecuaciones", Icons.Outlined.Functions),
    THEORY("Aprender", Icons.Outlined.School),
    AI_TUTOR("Tutor IA", Icons.Outlined.AutoAwesome),
    HISTORY("Historial", Icons.Outlined.History)
}

class MainActivity : ComponentActivity() {
    private val viewModel: PrecalcViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            var currentDestination by remember { mutableStateOf(AppDestination.CALCULATOR) }
            var showOverviewDialog by remember { mutableStateOf(false) }

            if (showOverviewDialog) {
                AppOverviewDialog(
                    onDismiss = { showOverviewDialog = false },
                    onNavigateTo = { dest ->
                        currentDestination = dest
                    }
                )
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    topBar = {
                        // Bento Grid Styled App Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Brand Icon + Title/Subtitle Group
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.clickable { currentDestination = AppDestination.CALCULATOR }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Nord8)
                                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp), spotColor = Nord8.copy(alpha = 0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Functions,
                                        contentDescription = "Logo",
                                        tint = Nord0,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "CalcPro",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "PRECALCULUS MASTER",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.2.sp,
                                        color = Nord9
                                    )
                                }
                            }

                            // Header Action Buttons: Guía de funciones & Selector de tema
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = { showOverviewDialog = true },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                                        .testTag("header_info_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = "Guía de Funciones",
                                        tint = Nord8,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.toggleTheme() },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                                        .testTag("theme_toggle_button")
                                ) {
                                    Icon(
                                        imageVector = if (isDarkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                                        contentDescription = "Cambiar tema",
                                        tint = Nord8,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    },
                    bottomBar = {
                        // Bento Rounded Bottom Navigation Bar
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                                ),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 16.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(
                                    AppDestination.CALCULATOR,
                                    AppDestination.STATISTICS,
                                    AppDestination.CAMERA_MATH,
                                    AppDestination.SOLVER,
                                    AppDestination.AI_TUTOR
                                ).forEach { destination ->
                                    val isSelected = currentDestination == destination
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(2.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { currentDestination = destination }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .testTag("nav_${destination.name.lowercase()}")
                                    ) {
                                        Icon(
                                            imageVector = destination.icon,
                                            contentDescription = destination.label,
                                            tint = if (isSelected) Nord8 else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Text(
                                            text = destination.label.uppercase(),
                                            fontSize = 8.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            letterSpacing = 0.5.sp,
                                            color = if (isSelected) Nord8 else MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentDestination,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "ScreenTransition"
                        ) { destination ->
                            when (destination) {
                                AppDestination.CALCULATOR -> CalculatorScreen(
                                    viewModel = viewModel,
                                    onNavigateToSolver = { currentDestination = AppDestination.SOLVER },
                                    onNavigateToStatistics = { currentDestination = AppDestination.STATISTICS },
                                    modifier = Modifier.fillMaxSize()
                                )
                                AppDestination.STATISTICS -> StatisticsScreen(
                                    viewModel = viewModel,
                                    onNavigateToTutor = { tutorPrompt ->
                                        viewModel.setAiPrompt(tutorPrompt)
                                        viewModel.askAiTutor(tutorPrompt)
                                        currentDestination = AppDestination.AI_TUTOR
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                                AppDestination.CAMERA_MATH -> CameraMathSolverScreen(
                                    viewModel = viewModel,
                                    onNavigateToGraph = { plotExpr ->
                                        viewModel.setExpression(plotExpr)
                                        currentDestination = AppDestination.CALCULATOR
                                    },
                                    onNavigateToSolver = { eqStr ->
                                        viewModel.setEquationInput(eqStr)
                                        viewModel.solveCurrentEquation()
                                        currentDestination = AppDestination.SOLVER
                                    },
                                    onNavigateToInverse = { fnStr ->
                                        viewModel.setInverseInput(fnStr)
                                        viewModel.solveCurrentInverse()
                                        currentDestination = AppDestination.SOLVER
                                    },
                                    onNavigateToTutor = { tutorPrompt ->
                                        viewModel.setAiPrompt(tutorPrompt)
                                        viewModel.askAiTutor(tutorPrompt)
                                        currentDestination = AppDestination.AI_TUTOR
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                                AppDestination.SOLVER -> EquationSolverScreen(
                                    viewModel = viewModel,
                                    onNavigateToGraph = { plotExpr ->
                                        viewModel.setExpression(plotExpr)
                                        currentDestination = AppDestination.CALCULATOR
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                                AppDestination.THEORY -> TopicExplorerScreen(
                                    onLoadFunction = { fnExpr ->
                                        viewModel.setExpression(fnExpr)
                                        currentDestination = AppDestination.CALCULATOR
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                                AppDestination.AI_TUTOR -> AiTutorScreen(
                                    viewModel = viewModel,
                                    onNavigateToCamera = { currentDestination = AppDestination.CAMERA_MATH },
                                    modifier = Modifier.fillMaxSize()
                                )
                                AppDestination.HISTORY -> HistoryFavoritesScreen(
                                    viewModel = viewModel,
                                    onSelectExpression = { expr, type ->
                                        if (type == "FUNCIÓN") {
                                            viewModel.setExpression(expr)
                                            currentDestination = AppDestination.CALCULATOR
                                        } else {
                                            viewModel.setEquationInput(expr)
                                            viewModel.solveCurrentEquation()
                                            currentDestination = AppDestination.SOLVER
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

