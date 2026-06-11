package ci.nsu.mobile.main.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ci.nsu.mobile.main.data.network.RetrofitClient
import ci.nsu.mobile.main.data.repository.AuthRepository
import ci.nsu.mobile.main.data.storage.TokenManager
import ci.nsu.mobile.main.ui.theme.PracticeTheme
import ci.nsu.mobile.main.ui.viewmodel.AuthViewModel
import ci.nsu.mobile.main.ui.viewmodel.MainViewModel
import ci.nsu.mobile.main.ui.viewmodel.RegisterViewModel

class MainActivity : ComponentActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var authRepository: AuthRepository
    private lateinit var authViewModel: AuthViewModel
    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация зависимостей
        tokenManager = TokenManager(applicationContext)
        val apiService = RetrofitClient.create(tokenManager)
        authRepository = AuthRepository(apiService, tokenManager)

        // Создание ViewModels
        authViewModel = AuthViewModel(authRepository)
        registerViewModel = RegisterViewModel(authRepository)
        mainViewModel = MainViewModel(authRepository)

        setContent {
            PracticeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        authViewModel = authViewModel,
                        registerViewModel = registerViewModel,
                        mainViewModel = mainViewModel,
                        authRepository = authRepository
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    registerViewModel: RegisterViewModel,
    mainViewModel: MainViewModel,
    authRepository: AuthRepository
) {
    val navController = rememberNavController()
    var isAuthenticated by remember { mutableStateOf(authRepository.checkAuth()) }

    // Функция для выхода
    fun handleLogout() {
        mainViewModel.logout()
        isAuthenticated = false
        navController.navigate("login") {
            popUpTo("main") { inclusive = true }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (isAuthenticated) "main" else "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    isAuthenticated = true
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                viewModel = authViewModel
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.popBackStack()
                },
                viewModel = registerViewModel
            )
        }

        composable("main") {
            MainScreen(
                onLogout = { handleLogout() },
                viewModel = mainViewModel
            )
        }
    }
}