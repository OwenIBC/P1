package com.example.p1

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.p1.databinding.ActivityMainBinding

/**
 * Activity única: aloja la Toolbar, el NavHostFragment y la BottomNavigationView
 * (Inicio · Historial · Configuración). Toda la lógica vive en los fragmentos/ViewModels.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Barra de estado con iconos claros sobre la AppBar carmesí.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyWindowInsets(binding)
        setupNavigation(binding)
    }

    private fun setupNavigation(binding: ActivityMainBinding) {
        // findFragmentById es la forma segura de obtener el NavController desde onCreate.
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as? NavHostFragment ?: return
        val navController = navHostFragment.navController

        // Las tres pestañas son destinos de nivel superior (sin flecha "atrás").
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.historyFragment, R.id.settingsFragment),
        )
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.bottomNavigation.setupWithNavController(navController)
    }

    /**
     * Edge-to-edge (obligatorio con targetSdk ≥ 35):
     *  - La AppBar recibe el inset superior (status bar).
     *  - Con el teclado abierto se oculta la BottomNavigationView y el contenido se
     *    redimensiona por encima del IME, para que ningún campo quede tapado.
     */
    private fun applyWindowInsets(binding: ActivityMainBinding) {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { root, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom

            binding.appBarLayout.updatePadding(top = bars.top)
            root.updatePadding(left = bars.left, right = bars.right, bottom = if (imeVisible) imeBottom else 0)
            binding.bottomNavigation.isVisible = !imeVisible
            insets // sin consumir: BottomNavigationView aplica su propio inset inferior
        }
    }
}