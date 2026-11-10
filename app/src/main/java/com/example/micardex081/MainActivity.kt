
package com.example.micardex081

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.micardex081.data.CardexDatabase
import com.example.micardex081.data.Materia
import kotlinx.coroutines.launch
import java.util.Locale

private val FondoCardex = Color(0xFFF4E2D8)
private val VinoCardex = Color(0xFF8B0000)
private val VerdeCardex = Color(0xFF2C5E1A)

private val tiposRegistro = listOf(
    "MATERIA",
    "VERANO",
    "RESIDENCIA",
    "SERVICIO_SOCIAL"
)

private fun nombreTipo(tipo: String): String = when (tipo) {
    "VERANO" -> "Materia de verano"
    "RESIDENCIA" -> "Residencia profesional"
    "SERVICIO_SOCIAL" -> "Servicio social"
    else -> "Materia normal"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = CardexDatabase.obtenerInstancia(applicationContext)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = FondoCardex
                ) {
                    CardexApp(db)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardexApp(db: CardexDatabase) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val preferencias = remember {
        context.getSharedPreferences("datos_alumno", Context.MODE_PRIVATE)
    }

    var nombreAlumno by remember {
        mutableStateOf(preferencias.getString("nombre", "") ?: "")
    }

    var controlAlumno by remember {
        mutableStateOf(preferencias.getString("control", "") ?: "")
    }

    var pantalla by remember { mutableIntStateOf(0) }
    var materias by remember { mutableStateOf<List<Materia>>(emptyList()) }

    var mostrarFormulario by remember { mutableStateOf(false) }
    var registroEditar by remember { mutableStateOf<Materia?>(null) }
    var tipoFormulario by remember { mutableStateOf("MATERIA") }

    var mensajeMaterias by remember { mutableStateOf("") }
    var mensajeOtros by remember { mutableStateOf("") }
    var mensajeAlumno by remember { mutableStateOf("") }

    fun recargarMaterias() {
        scope.launch {
            materias = db.materiaDao().obtenerTodas()
        }
    }

    fun abrirFormulario(tipo: String, registro: Materia? = null) {
        registroEditar = registro
        tipoFormulario = registro?.tipo ?: tipo

        if (tipoFormulario !in tiposRegistro) {
            tipoFormulario = tipo
        }

        if (tipoFormulario == "MATERIA") {
            mensajeMaterias = ""
        } else {
            mensajeOtros = ""
        }

        mostrarFormulario = true
    }

    LaunchedEffect(Unit) {
        recargarMaterias()
    }

    val materiasNormales = materias.filter { it.tipo == "MATERIA" }
    val registrosAdicionales = materias.filter { it.tipo != "MATERIA" }

    Scaffold(
        containerColor = FondoCardex,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Mi Cardex 081",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Historial académico",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VinoCardex
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = VinoCardex) {
                val etiquetas = listOf(
                    "Inicio", "Materias", "Consultas", "Otros", "Alumno"
                )
                val iconos = listOf("⌂", "▤", "∑", "＋", "☺")

                etiquetas.forEachIndexed { index, etiqueta ->
                    NavigationBarItem(
                        selected = pantalla == index,
                        onClick = {
                            pantalla = index
                            recargarMaterias()
                        },
                        icon = { Text(iconos[index], color = Color.White) },
                        label = { Text(etiqueta, color = Color.White) }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when (pantalla) {
                0 -> InicioScreen(
                    nombre = nombreAlumno,
                    control = controlAlumno,
                    materias = materias,
                    alAbrirMaterias = { pantalla = 1 },
                    alAbrirConsultas = { pantalla = 2 },
                    alAbrirOtros = { pantalla = 3 }
                )

                1 -> MateriasScreen(
                    materias = materiasNormales,
                    mensaje = mensajeMaterias,
                    alAgregar = { abrirFormulario("MATERIA") },
                    alEditar = { abrirFormulario("MATERIA", it) },
                    alEliminar = { registro ->
                        scope.launch {
                            db.materiaDao().eliminar(registro)
                            recargarMaterias()
                            mensajeMaterias = "Materia eliminada correctamente."
                        }
                    }
                )

                2 -> ConsultasScreen(materias)

                3 -> OtrosScreen(
                    registros = registrosAdicionales,
                    mensaje = mensajeOtros,
                    alAgregar = { abrirFormulario(it) },
                    alEditar = { abrirFormulario(it.tipo, it) },
                    alEliminar = { registro ->
                        scope.launch {
                            db.materiaDao().eliminar(registro)
                            recargarMaterias()
                            mensajeOtros = "Registro eliminado correctamente."
                        }
                    }
                )

                4 -> AlumnoScreen(
                    nombre = nombreAlumno,
                    control = controlAlumno,
                    mensaje = mensajeAlumno,
                    alGuardar = { nombre, control ->
                        nombreAlumno = nombre
                        controlAlumno = control

                        preferencias.edit()
                            .putString("nombre", nombre)
                            .putString("control", control)
                            .apply()

                        mensajeAlumno = "Datos guardados correctamente."
                    }
                )
            }
        }
    }

    if (mostrarFormulario) {
        FormularioRegistroDialog(
            registro = registroEditar,
            tipoInicial = tipoFormulario,
            alCerrar = { mostrarFormulario = false },
            alGuardar = { nombre, semestre, tipo, normal, repeticion, especial ->
                scope.launch {
                    if (registroEditar == null) {
                        db.materiaDao().insertar(
                            Materia(
                                nombreMateria = nombre,
                                semestre = semestre,
                                normal = normal,
                                repeticion = repeticion,
                                especial = especial,
                                tipo = tipo
                            )
                        )
                    } else {
                        db.materiaDao().actualizar(
                            registroEditar!!.copy(
                                nombreMateria = nombre,
                                semestre = semestre,
                                normal = normal,
                                repeticion = repeticion,
                                especial = especial,
                                tipo = tipo
                            )
                        )
                    }

                    materias = db.materiaDao().obtenerTodas()

                    if (tipo == "MATERIA") {
                        mensajeMaterias = "Materia guardada correctamente."
                    } else {
                        mensajeOtros = "Registro guardado correctamente."
                    }

                    mostrarFormulario = false
                }
            }
        )
    }
}

@Composable
fun InicioScreen(
    nombre: String,
    control: String,
    materias: List<Materia>,
    alAbrirMaterias: () -> Unit,
    alAbrirConsultas: () -> Unit,
    alAbrirOtros: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "¡Bienvenido!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = VinoCardex
        )

        Text(if (nombre.isBlank()) "Estudiante" else "Hola, $nombre")

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(18.dp)) {
                Text("RESUMEN ACADÉMICO", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text("Número de control: ${control.ifBlank { "No registrado" }}")
                Text("Materias normales: ${materias.count { it.tipo == "MATERIA" }}")
                Text("Materias de verano: ${materias.count { it.tipo == "VERANO" }}")
                Text("Residencia: ${materias.count { it.tipo == "RESIDENCIA" }}")
                Text("Servicio social: ${materias.count { it.tipo == "SERVICIO_SOCIAL" }}")

                Spacer(Modifier.height(8.dp))
                Text(
                    "Promedio general: ${formatear(calcularPromedio(materias))}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = VerdeCardex
                )
            }
        }

        Button(onClick = alAbrirMaterias, modifier = Modifier.fillMaxWidth()) {
            Text("Administrar materias")
        }

        OutlinedButton(
            onClick = alAbrirConsultas,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Consultar promedios")
        }

        OutlinedButton(
            onClick = alAbrirOtros,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Veranos, residencia y servicio social")
        }
    }
}

@Composable
fun MateriasScreen(
    materias: List<Materia>,
    mensaje: String,
    alAgregar: () -> Unit,
    alEditar: (Materia) -> Unit,
    alEliminar: (Materia) -> Unit
) {
    var filtro by remember { mutableStateOf("") }
    val filtradas = materias.filter {
        it.nombreMateria.contains(filtro, ignoreCase = true)
    }

    Column(Modifier.fillMaxSize()) {
        Text(
            "Mis materias",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = VinoCardex
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = filtro,
            onValueChange = { filtro = it },
            label = { Text("Buscar materia") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        Button(onClick = alAgregar, modifier = Modifier.fillMaxWidth()) {
            Text("Agregar materia")
        }

        if (mensaje.isNotBlank()) Text(mensaje, color = VerdeCardex)
        Text("Total: ${filtradas.size}")

        if (filtradas.isEmpty()) {
            Text("No hay materias normales registradas.")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtradas, key = { it.id }) { materia ->
                    TarjetaRegistro(
                        materia,
                        { alEditar(materia) },
                        { alEliminar(materia) }
                    )
                }
            }
        }
    }
}

@Composable
fun OtrosScreen(
    registros: List<Materia>,
    mensaje: String,
    alAgregar: (String) -> Unit,
    alEditar: (Materia) -> Unit,
    alEliminar: (Materia) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Text(
            "Registros adicionales",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = VinoCardex
        )

        Spacer(Modifier.height(8.dp))
        Text("Estos registros se guardan fuera de los semestres regulares.")

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { alAgregar("VERANO") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Agregar materia de verano")
        }

        OutlinedButton(
            onClick = { alAgregar("RESIDENCIA") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Agregar residencia profesional")
        }

        OutlinedButton(
            onClick = { alAgregar("SERVICIO_SOCIAL") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Agregar servicio social")
        }

        if (mensaje.isNotBlank()) Text(mensaje, color = VerdeCardex)
        Text("Registros guardados: ${registros.size}")

        if (registros.isEmpty()) {
            Text("Todavía no hay registros adicionales.")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(registros, key = { it.id }) { registro ->
                    TarjetaRegistro(
                        registro,
                        { alEditar(registro) },
                        { alEliminar(registro) }
                    )
                }
            }
        }
    }
}

@Composable
fun TarjetaRegistro(
    registro: Materia,
    alEditar: () -> Unit,
    alEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                registro.nombreMateria,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = VinoCardex
            )

            Text(nombreTipo(registro.tipo))
            Text(
                if (registro.tipo == "MATERIA") {
                    "Semestre: ${registro.semestre}"
                } else {
                    "Fuera de semestre"
                }
            )

            Text("Normal: ${registro.normal?.let { formatear(it) } ?: "Pendiente"}")

            if (registro.tipo == "MATERIA") {
                Text("Repetición: ${registro.repeticion?.let { formatear(it) } ?: "Pendiente"}")
                Text("Especial: ${registro.especial?.let { formatear(it) } ?: "Pendiente"}")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = alEditar,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Editar")
                }

                Button(
                    onClick = alEliminar,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Eliminar")
                }
            }
        }
    }
}

@Composable
fun FormularioRegistroDialog(
    registro: Materia?,
    tipoInicial: String,
    alCerrar: () -> Unit,
    alGuardar: (String, Int, String, Double?, Double?, Double?) -> Unit
) {
    var nombre by remember(registro, tipoInicial) {
        mutableStateOf(registro?.nombreMateria ?: "")
    }

    var semestreTexto by remember(registro, tipoInicial) {
        mutableStateOf(
            when {
                registro != null && registro.tipo == "MATERIA" ->
                    registro.semestre.toString()
                registro != null -> "0"
                tipoInicial == "MATERIA" -> ""
                else -> "0"
            }
        )
    }

    var tipo by remember(registro, tipoInicial) {
        mutableStateOf(registro?.tipo ?: tipoInicial)
    }

    var normalTexto by remember(registro) {
        mutableStateOf(registro?.normal?.toString() ?: "")
    }

    var repeticionTexto by remember(registro) {
        mutableStateOf(registro?.repeticion?.toString() ?: "")
    }

    var especialTexto by remember(registro) {
        mutableStateOf(registro?.especial?.toString() ?: "")
    }

    var error by remember { mutableStateOf("") }
    val esMateriaNormal = tipo == "MATERIA"

    AlertDialog(
        onDismissRequest = alCerrar,
        title = { Text(if (registro == null) "Nuevo registro" else "Editar registro") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Tipo de registro", fontWeight = FontWeight.Bold)

                tiposRegistro.forEach { opcion ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        RadioButton(
                            selected = tipo == opcion,
                            onClick = {
                                tipo = opcion
                                semestreTexto = if (opcion == "MATERIA") "1" else "0"
                                error = ""
                            }
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 12.dp)
                        ) {
                            Text(nombreTipo(opcion))
                        }
                    }
                }

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = {
                        Text(
                            when (tipo) {
                                "RESIDENCIA" -> "Nombre o proyecto de residencia"
                                "SERVICIO_SOCIAL" -> "Nombre del servicio social"
                                else -> "Nombre de la materia"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (esMateriaNormal) {
                    OutlinedTextField(
                        value = semestreTexto,
                        onValueChange = { semestreTexto = it },
                        label = { Text("Semestre (1 a 12)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                } else {
                    Text("Este registro no pertenece a un semestre.", color = VerdeCardex)
                }

                OutlinedTextField(
                    value = normalTexto,
                    onValueChange = { normalTexto = it },
                    label = {
                        Text(
                            when (tipo) {
                                "VERANO" -> "Calificación de verano"
                                "RESIDENCIA" -> "Calificación de residencia"
                                "SERVICIO_SOCIAL" -> "Calificación de servicio social"
                                else -> "Calificación normal"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (esMateriaNormal) {
                    OutlinedTextField(
                        value = repeticionTexto,
                        onValueChange = { repeticionTexto = it },
                        label = { Text("Calificación de repetición") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = especialTexto,
                        onValueChange = { especialTexto = it },
                        label = { Text("Calificación especial") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Text("Puedes dejar la calificación vacía si está pendiente.")

                if (error.isNotBlank()) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val semestre = if (tipo == "MATERIA") {
                        semestreTexto.toIntOrNull()
                    } else {
                        0
                    }

                    val normal = convertirCalificacion(normalTexto)
                    val repeticion = if (esMateriaNormal) convertirCalificacion(repeticionTexto) else null
                    val especial = if (esMateriaNormal) convertirCalificacion(especialTexto) else null

                    val textosValidos =
                        (normalTexto.isBlank() || normal != null) &&
                                (!esMateriaNormal || repeticionTexto.isBlank() || repeticion != null) &&
                                (!esMateriaNormal || especialTexto.isBlank() || especial != null)

                    val calificacionesValidas =
                        listOfNotNull(normal, repeticion, especial).all { it in 0.0..100.0 }

                    when {
                        nombre.isBlank() ->
                            error = "Escribe el nombre del registro."

                        semestre == null || (tipo == "MATERIA" && semestre !in 1..12) ->
                            error = "El semestre debe estar entre 1 y 12."

                        !textosValidos ->
                            error = "Escribe calificaciones numéricas válidas."

                        !calificacionesValidas ->
                            error = "Las calificaciones deben estar entre 0 y 100."

                        else -> alGuardar(
                            nombre.trim(),
                            semestre,
                            tipo,
                            normal,
                            repeticion,
                            especial
                        )
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = alCerrar) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ConsultasScreen(materias: List<Materia>) {
    var semestreTexto by remember { mutableStateOf("1") }
    val semestre = semestreTexto.toIntOrNull()

    val materiasSemestre = if (semestre != null && semestre in 1..12) {
        materias.filter { it.tipo == "MATERIA" && it.semestre == semestre }
    } else {
        emptyList()
    }

    val materiasVerano = materias.filter { it.tipo == "VERANO" }
    val residencias = materias.filter { it.tipo == "RESIDENCIA" }
    val serviciosSociales = materias.filter { it.tipo == "SERVICIO_SOCIAL" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Consultas y promedios",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = VinoCardex
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("PROMEDIO GENERAL", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    formatear(calcularPromedio(materias)),
                    style = MaterialTheme.typography.headlineMedium,
                    color = VerdeCardex,
                    fontWeight = FontWeight.Bold
                )
                Text("Incluye todas las calificaciones numéricas registradas.")
            }
        }

        OutlinedTextField(
            value = semestreTexto,
            onValueChange = { semestreTexto = it },
            label = { Text("Número de semestre") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (semestre == null || semestre !in 1..12) {
            Text("Escribe un semestre válido del 1 al 12.")
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("SEMESTRE $semestre", fontWeight = FontWeight.Bold)
                    Text("Materias: ${materiasSemestre.size}")
                    Text(
                        "Promedio: ${formatear(calcularPromedio(materiasSemestre))}",
                        style = MaterialTheme.typography.titleLarge,
                        color = VerdeCardex,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            materiasSemestre.forEach { materia ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(materia.nombreMateria, fontWeight = FontWeight.Bold)
                        Text("Promedio: ${formatear(calcularPromedio(listOf(materia)))}")
                    }
                }
            }
        }

        SeccionPromedioAdicional("MATERIAS DE VERANO", materiasVerano)
        SeccionPromedioAdicional("RESIDENCIA PROFESIONAL", residencias)
        SeccionPromedioAdicional("SERVICIO SOCIAL", serviciosSociales)
    }
}

@Composable
fun SeccionPromedioAdicional(titulo: String, registros: List<Materia>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(titulo, fontWeight = FontWeight.Bold)

            if (registros.isEmpty()) {
                Text("No hay registros.")
            } else {
                registros.forEach { registro ->
                    Text(
                        "${registro.nombreMateria}: ${
                            registro.normal?.let { formatear(it) } ?: "Pendiente"
                        }"
                    )
                }

                Text(
                    "Promedio: ${formatear(calcularPromedio(registros))}",
                    color = VerdeCardex,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AlumnoScreen(
    nombre: String,
    control: String,
    mensaje: String,
    alGuardar: (String, String) -> Unit
) {
    var nombreTexto by remember(nombre) { mutableStateOf(nombre) }
    var controlTexto by remember(control) { mutableStateOf(control) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "Datos del alumno",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = VinoCardex
        )

        OutlinedTextField(
            value = nombreTexto,
            onValueChange = { nombreTexto = it },
            label = { Text("Nombre completo") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = controlTexto,
            onValueChange = { controlTexto = it },
            label = { Text("Número de control") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = { alGuardar(nombreTexto.trim(), controlTexto.trim()) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar datos")
        }

        if (mensaje.isNotBlank()) Text(mensaje, color = VerdeCardex)
    }
}

private fun convertirCalificacion(texto: String): Double? {
    if (texto.isBlank()) return null
    return texto.trim().replace(',', '.').toDoubleOrNull()
}

private fun calcularPromedio(materias: List<Materia>): Double {
    val calificaciones = materias.flatMap { materia ->
        listOfNotNull(
            materia.normal,
            materia.repeticion,
            materia.especial
        )
    }

    val suma = calificaciones.sum()
    val cantidad = calificaciones.size
    val promedio = if (cantidad == 0) 0.0 else suma / cantidad

    Log.d("CARDEX_PROMEDIO", "CALIFICACIONES CONTABILIZADAS: $cantidad")
    Log.d("CARDEX_PROMEDIO", "SUMA TOTAL: $suma")
    Log.d("CARDEX_PROMEDIO", "PROMEDIO CALCULADO: $promedio")

    return promedio
}

private fun formatear(valor: Double): String {
    return String.format(Locale.getDefault(), "%.2f", valor)
}
