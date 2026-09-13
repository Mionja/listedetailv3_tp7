package mg.itu.listedetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Mini-TP 7 — le ViewModel, désormais branché sur la base Room.
 *
 * Ce qui change par rapport à la séance 6 : l'état ne vient plus d'une liste
 * en mémoire, mais de la BASE — via des Flow qui ré-émettent à chaque
 * changement. La base est la source de vérité ; l'écran n'en est qu'un reflet.
 *
 * Rien à modifier ici tant que les TODO du DAO ne sont pas écrits.
 * Ensuite : suivez le bloc « ÉTAPE 3 » pour brancher vos requêtes.
 */

enum class ModeAffichage { NOM, PRIX_DECROISSANT, STOCK_SUFFISANT }

data class EtatUi(
    val produits: List<Produit> = emptyList(),
    val mode: ModeAffichage = ModeAffichage.NOM,
    val stockTotal: Double? = null,
)

class ProduitsViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.obtenir(application).produitDao()
    private val mode = MutableStateFlow(ModeAffichage.NOM)

    init {
        // Premier lancement : on remplit la base si elle est vide.
        viewModelScope.launch {
            if (dao.parId(1) == null) dao.insererTous(produitsInitiaux)
        }
    }

    /**
     * L'état de l'écran = les produits (selon le mode choisi) + le stock total.
     * combine() fusionne plusieurs Flow en un seul : dès que l'un ré-émet,
     * l'état est recalculé et l'écran se recompose.
     */
    val uiState: StateFlow<EtatUi> =
        combine(
            dao.tousLesProduits(),
            mode,
        ) { produits, modeCourant ->
            EtatUi(produits = produits, mode = modeCourant)

            // ----------------------------------------------------------------
            // ÉTAPE 3 — brancher VOS requêtes (après les TODO du DAO)
            //
            // 1) Ajoutez vos Flow aux arguments de combine(), par exemple :
            //
            //      combine(
            //          dao.tousLesProduits(),
            //          dao.parPrixDecroissant(),       // votre TODO 1
            //          dao.stockSuperieurA(10.0),      // votre TODO 2
            //          dao.stockTotal(),               // votre TODO 3
            //          mode,
            //      ) { parNom, parPrix, stockOk, total, modeCourant ->
            //
            // 2) Choisissez la liste selon le mode :
            //
            //      val liste = when (modeCourant) {
            //          ModeAffichage.NOM -> parNom
            //          ModeAffichage.PRIX_DECROISSANT -> parPrix
            //          ModeAffichage.STOCK_SUFFISANT -> stockOk
            //      }
            //      EtatUi(produits = liste, mode = modeCourant, stockTotal = total)
            //
            // Objectif minimal : AU MOINS un mode réellement branché,
            // et le stock total affiché.
            // ----------------------------------------------------------------
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EtatUi(),
        )

    fun changerMode(nouveau: ModeAffichage) {
        mode.value = nouveau
    }
}
