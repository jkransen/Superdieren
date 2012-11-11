package models

import org.scalatest.junit.AssertionsForJUnit
import scala.collection.mutable.ListBuffer
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

class KaartTest extends AssertionsForJUnit {

  @Test def verifyKaartnummers() {
    val eersteKaart = Kaart(1)
    assert(eersteKaart.serie === 1)
    assert(eersteKaart.index === 1)
    val twaalfdeKaart = Kaart(12)
    assert(twaalfdeKaart.serie === 1)
    assert(twaalfdeKaart.index === 12)
    val dertiendeKaart = Kaart(13)
    assert(dertiendeKaart.serie === 2)
    assert(dertiendeKaart.index === 1)
    val laatsteKaart = Kaart(17 * 12)
    assert(laatsteKaart.serie === 17)
    assert(laatsteKaart.index === 12)
  }

  @Test def verifyApply() {
    val eersteKaart = Kaart(1,1)
    assert(eersteKaart.serie === 1)
    assert(eersteKaart.index === 1)
    val laatsteKaart = Kaart(17,12)
    assert(laatsteKaart.serie === 17)
    assert(laatsteKaart.index === 12)
  }

}