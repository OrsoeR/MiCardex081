
package com.example.micardex081

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private val MoradoPrincipal = Color(0xFF5E35B1)
private val FondoLavanda = Color(0xFFF8F5FF)

@Composable
fun CardexNavigation(
    pantallaInicio: @Composable () -> Unit,
    pantallaMaterias: @Composable () -> Unit,
    pantallaConsultas: @Composable () -> Unit
) {
    var seleccion by remember { mutableIntStateOf(0) }

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = MoradoPrincipal,
            background = FondoLavanda
        )
    ) {
        Scaffold(
            containerColor = FondoLavanda,
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White
                ) {
                    NavigationBarItem(
                        selected = seleccion == 0,
                        onClick = { seleccion = 0 },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = "Inicio"
                            )
                        },
                        label = { Text("Inicio") }
                    )

                    NavigationBarItem(
                        selected = seleccion == 1,
                        onClick = { seleccion = 1 },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Book,
                                contentDescription = "Materias"
                            )
                        },
                        label = { Text("Materias") }
                    )

                    NavigationBarItem(
                        selected = seleccion == 2,
                        onClick = { seleccion = 2 },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.BarChart,
                                contentDescription = "Consultas"
                            )
                        },
                        label = { Text("Consultas") }
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (seleccion) {
                    0 -> pantallaInicio()
                    1 -> pantallaMaterias()
                    2 -> pantallaConsultas()
                }
            }
        }
    }
}
