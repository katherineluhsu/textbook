// Ammonite script to identify frequent vocab items
// NOT included in vocab lists.

val personalRepo = coursierapi.MavenRepository.of("https://dl.bintray.com/neelsmith/maven")
interp.repositories() ++= Seq(personalRepo)

// ivy imports
import $ivy.`edu.holycross.shot::latincorpus:7.0.0-pr5`
import $ivy.`edu.holycross.shot::histoutils:2.3.0`
import $ivy.`edu.holycross.shot::tabulae:7.0.5`

import scala.io.Source
import edu.holycross.shot.latincorpus._
import edu.holycross.shot.histoutils._
import edu.holycross.shot.tabulae._

val hyginusUrl = "https://raw.githubusercontent.com/LinguaLatina/analysis/master/data/hyginus/hyginus-latc.cex"
val hyginus = LatinCorpus.fromUrl(hyginusUrl)

val lcCorpus = LatinCorpus(hyginus.tokens.filter(_.text.head.isLower))
val lexemesHist = lcCorpus.lexemesHistogram
val lsLexemesHist = Histogram(lexemesHist.frequencies.filterNot(_.item.startsWith("composites")))





val vocabFiles : Map[Int, String] = Map(
  1 -> "https://raw.githubusercontent.com/LinguaLatina/textbook/master/vocablists/01-nouns-adjs-pron.cex",
  2 -> "https://raw.githubusercontent.com/LinguaLatina/textbook/master/vocablists/02-verbs.cex",
  3 -> "https://raw.githubusercontent.com/LinguaLatina/textbook/master/vocablists/03-place-and-time.cex",
  4 -> "https://raw.githubusercontent.com/LinguaLatina/textbook/master/vocablists/04-verbal-nouns-and-adjectives.cex"
)

// collect cumulative vocabulary IDs through a given unit:
def coverageForUnit(vocabUnit: Int): Vector[String] = {
  val vocab = for (i <- 1 to vocabUnit) yield {
    val lines = Source.fromURL(vocabFiles(i))
    val lexemeIds = lines.getLines.toVector.tail.filter(_.nonEmpty).map( ln => {
      val columns = ln.split("#")
      val idParts = columns.head.split(":")
      idParts.head
    })
    lexemeIds
  }
  vocab.toVector.flatten
}

val vocabList = coverageForUnit(4)


vocabList.size
val top80pct = lsLexemesHist.takePercent(80)

val notCovered = top80pct.filterNot( freq => vocabList.contains(freq.item))



val labelled = notCovered.map(f => Frequency(LewisShort.label(f.item), f.count))




import java.io.PrintWriter
new PrintWriter("not-covered.cex") {write(labelled.map(_.cex()).mkString("\n")); close;}
