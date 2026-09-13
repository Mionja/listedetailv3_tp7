package mg.itu.listedetail

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

/**
 * Mini-TP 7 — « Trois requêtes »
 *
 * La couche données de l'application, avec Room.
 * Trois éléments à connaître, et c'est tout :
 *   - l'ENTITY  : une table, décrite par une data class
 *   - le DAO    : les requêtes, décrites par des fonctions annotées
 *   - la DATABASE : le point d'assemblage
 *
 * Votre travail : les TROIS TODO du DAO. Rien d'autre n'est à modifier.
 */

// ---------------------------------------------------------------------------
// L'ENTITY — une table « produits », une ligne par produit
// ---------------------------------------------------------------------------

@Entity(tableName = "produits")
data class Produit(
    @PrimaryKey val id: Int,
    val nom: String,
    val origine: String,
    val prixKg: Double?,      // null = prix non fixé : la nullabilité va jusqu'en base
    val stockKg: Double,
)

// ---------------------------------------------------------------------------
// LE DAO — les requêtes (3 TODO)
// ---------------------------------------------------------------------------

@Dao
interface ProduitDao {

    /** Fourni : tous les produits, triés par nom. Un Flow : ré-émet à chaque changement. */
    @Query("SELECT * FROM produits ORDER BY nom ASC")
    fun tousLesProduits(): Flow<List<Produit>>

    /** Fourni : un produit par son identifiant (utilisé par l'écran de détail). */
    @Query("SELECT * FROM produits WHERE id = :id")
    suspend fun parId(id: Int): Produit?

    /** Fourni : insertion du jeu de données initial. */
    @Insert
    suspend fun insererTous(produits: List<Produit>)

    // -----------------------------------------------------------------------
    // TODO 1 — TRI : les produits triés du plus cher au moins cher.
    // Attention : les produits sans prix (NULL) doivent apparaître EN DERNIER.
    // Indice SQL : ORDER BY prixKg IS NULL, prixKg DESC
    // Signature à écrire :
    //     @Query("...")
    //     fun parPrixDecroissant(): Flow<List<Produit>>
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    // TODO 2 — FILTRE : les produits dont le stock dépasse un seuil donné,
    // le seuil étant un paramètre de la fonction (syntaxe :nomDuParametre).
    // Signature à écrire :
    //     @Query("...")
    //     fun stockSuperieurA(seuilKg: Double): Flow<List<Produit>>
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    // TODO 3 — AGRÉGAT : le stock TOTAL de tous les produits, en une valeur.
    // Indice SQL : SELECT SUM(stockKg) FROM produits
    // Signature à écrire (le résultat peut être null si la table est vide) :
    //     @Query("...")
    //     fun stockTotal(): Flow<Double?>
    // -----------------------------------------------------------------------
}

// ---------------------------------------------------------------------------
// LA DATABASE — le point d'assemblage (rien à modifier)
// ---------------------------------------------------------------------------

@Database(entities = [Produit::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun produitDao(): ProduitDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun obtenir(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cooperative.db",
                )
                    // Pour ce TP : si le schéma change, on repart d'une base neuve.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}

/** Jeu de données initial, inséré au premier lancement. */
val produitsInitiaux = listOf(
    Produit(1, "Vanille Bourbon", "Sambava", 250_000.0, 18.5),
    Produit(2, "Café Arabica", "Itasy", 12_000.0, 42.0),
    Produit(3, "Girofle", "Analanjirofo", 38_000.0, 15.0),
    Produit(4, "Litchi", "Toamasina", null, 55.0),
    Produit(5, "Poivre noir", "Vatovavy", 45_000.0, 7.5),
)
