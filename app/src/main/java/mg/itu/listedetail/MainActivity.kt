package mg.itu.listedetail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * Mini-TP 7 — « Trois requêtes »
 *
 * L'application des séances 5 et 6, désormais branchée sur une base Room.
 * Les écrans et la navigation sont COMPLETS : votre travail est dans
 * Donnees.kt (les trois TODO du DAO), puis dans ProduitsViewModel.kt
 * (l'étape 3 : brancher vos requêtes).
 */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: ProduitsViewModel = viewModel()

    NavHost(navController = navController, startDestination = "liste") {

        composable("liste") {
            EcranListe(
                viewModel = viewModel,
                onProduitClick = { produitId -> navController.navigate("detail/$produitId") },
            )
        }

        composable("detail/{produitId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("produitId")?.toIntOrNull()
            val etat by viewModel.uiState.collectAsState()
            val produit = etat.produits.find { it.id == id }
            if (produit != null) {
                EcranDetail(produit = produit, onRetour = { navController.popBackStack() })
            }
        }
    }
}

@Composable
fun EcranListe(
    viewModel: ProduitsViewModel,
    onProduitClick: (Int) -> Unit,
) {
    val etat by viewModel.uiState.collectAsState()

    Column(Modifier.padding(16.dp)) {
        Text("Produits de la coopérative", style = MaterialTheme.typography.headlineSmall)

        // Le stock total vient de VOTRE requête d'agrégat (TODO 3).
        Text(
            etat.stockTotal?.let { "Stock total : $it kg" }
                ?: "Stock total : (TODO 3 — non branché)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(8.dp))

        // Les trois modes d'affichage : chacun s'appuie sur une requête.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ModeAffichage.entries.forEach { m ->
                FilterChip(
                    selected = etat.mode == m,
                    onClick = { viewModel.changerMode(m) },
                    label = {
                        Text(
                            when (m) {
                                ModeAffichage.NOM -> "Nom"
                                ModeAffichage.PRIX_DECROISSANT -> "Prix ↓"
                                ModeAffichage.STOCK_SUFFISANT -> "Stock > 10"
                            }
                        )
                    },
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        LazyColumn {
            items(etat.produits) { p ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onProduitClick(p.id) },
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(p.nom, style = MaterialTheme.typography.titleMedium)
                        Text(
                            p.prixKg?.let { "${formatAriary(it)} / kg" } ?: "prix non fixé",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text("Stock : ${p.stockKg} kg", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun EcranDetail(
    produit: Produit,
    onRetour: () -> Unit,
) {
    Column(Modifier.padding(24.dp)) {
        Text(produit.nom, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Origine : ${produit.origine}", style = MaterialTheme.typography.bodyLarge)
        Text(
            produit.prixKg?.let { "Prix : ${formatAriary(it)} / kg" } ?: "Prix non fixé",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text("Stock : ${produit.stockKg} kg", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetour) { Text("Retour à la liste") }
    }
}

/** Formate un montant en ariary : 1250000.0 -> "1 250 000 Ar". */
fun formatAriary(montant: Double): String {
    val entier = montant.toLong().toString()
    val groupes = entier.reversed().chunked(3).joinToString(" ").reversed()
    return "$groupes Ar"
}
