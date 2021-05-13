package example

import pl.msitko.refined.Refined
import pl.msitko.refined.Refined._
import pl.msitko.refined.auto._
import pl.msitko.refined.ValidateExpr._
import munit.Assertions.assert
import pl.msitko.refined.testUtils.CompileTimeSuite
import scala.language.implicitConversions

import scala.compiletime.testing.{typeCheckErrors => errors}

class IntSpec extends CompileTimeSuite {
  test("GreaterThan[10] should fail for lower or equal to to") {
    shouldContain(errors("mkValidatedInt[7, GreaterThan[10]](7)"),
                  "Validation failed: 7 > 10")
    shouldContain(errors("mkValidatedInt[10, GreaterThan[10]](10)"),
                  "Validation failed: 10 > 10")
  }
  test("GreaterThan[10] should fail for lower or equal to to (implicitly)") {
    shouldContain(errors("val a: Int Refined GreaterThan[10] = 7"),
                  "Validation failed: 7 > 10")
  }
  test("GreaterThan[10] should pass for greater than 10") {
    val a: Int Refined GreaterThan[10] = mkValidatedInt[16, GreaterThan[10]](16)
    val xy: Int = 45
    val xyz = xy + 32
    assertEquals(a + 0, 16)
  }
  test("GreaterThan[10] should pass for greater than 10 (implicitly)") {
    val a: Refined[Int, GreaterThan[10]] = 16
    assertEquals(a + 0, 16)
  }
  test("GreaterThan And LowerThan") {
    val a: Int Refined And[GreaterThan[10], LowerThan[20]] = 15
  }
  test("GreaterThan And LowerThan - failure") {
    shouldContain(errors("val a: Int Refined And[GreaterThan[10], LowerThan[20]] = 5"),
                  "Validation failed: (5 > 10 And 5 < 20), predicate failed: 5 > 10")
    assert(errors("val b: Int Refined And[GreaterThan[10], LowerThan[20]] = 25").nonEmpty)
    // Following assertion fail because `errors` returns:
    // The value of: a$proxy2
    // could not be extracted using scala.quoted.FromExpr$PrimitiveFromExpr@5f5131cc"""
    // I believe it's only an artifact of scala.compiletime.testing.typeCheckErrors, the message is as expected when trying to compile
//    failCompilationWith(errors("val b: Int Refined And[GreaterThan[10], LowerThan[20]] = 25"),
//                  "Validation failed: 25 < 20")
  }
  test("nested boolean conditions") {
    val a: Int Refined Or[And[GreaterThan[10], LowerThan[20]], And[GreaterThan[110], LowerThan[120]]] = 15
    val b: Int Refined Or[And[GreaterThan[10], LowerThan[20]], And[GreaterThan[110], LowerThan[120]]] = 115
  }
  test("nested boolean conditions - failure") {
    shouldContain(errors("val a: Int Refined Or[And[GreaterThan[10], LowerThan[20]], And[GreaterThan[110], LowerThan[120]]] = 5"),
                  "Validation failed: ((5 > 10 And 5 < 20) Or (5 > 110 And 5 < 120))")
    // Similarly to above comments, we cannot assert errors due to scala.compiletime.testing.typeCheckErrors limitations
    assert(errors("val a: Int Refined Or[And[GreaterThan[10], LowerThan[20]], And[GreaterThan[110], LowerThan[120]]] = 35").nonEmpty)
    assert(errors("val a: Int Refined Or[And[GreaterThan[10], LowerThan[20]], And[GreaterThan[110], LowerThan[120]]] = 125").nonEmpty)
  }
  test("basic inference (GreaterThan)") {
    val a: Int Refined GreaterThan[10] = 16
    val b: Int Refined GreaterThan[5] = a
    shouldContain(errors("val c: Int Refined GreaterThan[15] = a"),
            "Cannot be inferred")

  }
  test("basic inference (LowerThan)") {
    val a: Int Refined LowerThan[10] = 7
    val b: Int Refined LowerThan[15] = a
    shouldContain(errors("val c: Int Refined LowerThan[5] = a"),
            "Cannot be inferred")
  }
  test("Refined.unsafeApply should not compile outside of pl.msitko.refined package") {
    val es = errors("Refined.unsafeApply[34, GreaterThan[10]](34)")
    assert(clue(es.head.message).contains("none of the overloaded alternatives named unsafeApply can be accessed as a member"))
  }
}
