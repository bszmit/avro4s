package com.sksamuel.avro4s.record.decoder

import com.sksamuel.avro4s.{AvroName, AvroNamespace, AvroSchema, Decoder, DefaultFieldMapper, Encoder, ImmutableRecord}
import org.apache.avro.SchemaBuilder
import org.apache.avro.generic.GenericData
import org.apache.avro.util.Utf8
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

case class Test(either: Either[String, Double])
case class Goo(s: String)
case class Foo(b: Boolean)
case class Test2(either: Either[Goo, Foo])

class EitherDecoderTest extends AnyFunSuite with Matchers {

  import scala.collection.JavaConverters._

  case class Voo(s: String)
  case class Woo(b: Boolean)
  case class Test3(either: Either[Voo, Woo])

  @AvroName("w")
  case class Wobble(s: String)

  @AvroName("t")
  case class Topple(b: Boolean)

  case class Test4(either: Either[Wobble, Topple])

  @AvroNamespace("market")
  case class Apple(s: String)

  @AvroNamespace("market")
  case class Orange(b: Boolean)

  case class Test5(either: Either[Apple, Orange])

  case class Test6(either: Either[String, Seq[Double]])

  test("decode union:T,U for Either[T,U] of primitives") {
    val schema = AvroSchema[Test]
    Decoder[Test].decode(ImmutableRecord(schema, Vector(new Utf8("foo"))), schema, DefaultFieldMapper) shouldBe Test(Left("foo"))
    Decoder[Test].decode(ImmutableRecord(schema, Vector(java.lang.Double.valueOf(234.4D))), schema, DefaultFieldMapper) shouldBe Test(Right(234.4D))
  }

  test("decode union:T,U for Either[T,U] of top level classes") {
    val schema = AvroSchema[Test2]
    Decoder[Test2].decode(ImmutableRecord(schema, Vector(ImmutableRecord(AvroSchema[Goo], Vector(new Utf8("zzz"))))), schema, DefaultFieldMapper) shouldBe Test2(Left(Goo("zzz")))
    Decoder[Test2].decode(ImmutableRecord(schema, Vector(ImmutableRecord(AvroSchema[Foo], Vector(java.lang.Boolean.valueOf(true))))), schema, DefaultFieldMapper) shouldBe Test2(Right(Foo(true)))
  }

  test("decode union:T,U for Either[T,U] of nested classes") {
    val schema = AvroSchema[Test3]
    Decoder[Test3].decode(ImmutableRecord(schema, Vector(ImmutableRecord(AvroSchema[Voo], Vector(new Utf8("zzz"))))), schema, DefaultFieldMapper) shouldBe Test3(Left(Voo("zzz")))
    Decoder[Test3].decode(ImmutableRecord(schema, Vector(ImmutableRecord(AvroSchema[Woo], Vector(java.lang.Boolean.valueOf(true))))), schema, DefaultFieldMapper) shouldBe Test3(Right(Woo(true)))
  }

  test("use @AvroName defined on a class when choosing which Either to decode") {

    val wschema = SchemaBuilder.record("w").namespace("com.sksamuel.avro4s.record.decoder.EitherDecoderTest").fields().requiredBoolean("s").endRecord()
    val tschema = SchemaBuilder.record("t").namespace("com.sksamuel.avro4s.record.decoder.EitherDecoderTest").fields().requiredString("b").endRecord()
    val union = SchemaBuilder.unionOf().`type`(wschema).and().`type`(tschema).endUnion()
    val schema = SchemaBuilder.record("Test4").fields().name("either").`type`(union).noDefault().endRecord()

    Decoder[Test4].decode(ImmutableRecord(schema, Vector(ImmutableRecord(tschema, Vector(java.lang.Boolean.valueOf(true))))), schema, DefaultFieldMapper) shouldBe Test4(Right(Topple(true)))
    Decoder[Test4].decode(ImmutableRecord(schema, Vector(ImmutableRecord(wschema, Vector(new Utf8("zzz"))))), schema, DefaultFieldMapper) shouldBe Test4(Left(Wobble("zzz")))
  }

  test("use @AvroNamespace when choosing which Either to decode") {

    val appleschema = SchemaBuilder.record("Apple").namespace("market").fields().requiredBoolean("s").endRecord()
    val orangeschema = SchemaBuilder.record("Orange").namespace("market").fields().requiredString("b").endRecord()
    val union = SchemaBuilder.unionOf().`type`(appleschema).and().`type`(orangeschema).endUnion()
    val schema = SchemaBuilder.record("Test5").fields().name("either").`type`(union).noDefault().endRecord()

    Decoder[Test5].decode(ImmutableRecord(schema, Vector(ImmutableRecord(orangeschema, Vector(java.lang.Boolean.valueOf(true))))), schema, DefaultFieldMapper) shouldBe Test5(Right(Orange(true)))
    Decoder[Test5].decode(ImmutableRecord(schema, Vector(ImmutableRecord(appleschema, Vector(new Utf8("zzz"))))), schema, DefaultFieldMapper) shouldBe Test5(Left(Apple("zzz")))
  }

  test("decode union:T,U for Either[T,U] of primitive and array") {
    val schema = AvroSchema[Test6]
    val array = new GenericData.Array[java.lang.Double](AvroSchema[Seq[Double]], Seq(java.lang.Double.valueOf(234.4D), java.lang.Double.valueOf(235.5D)).asJava)

    Decoder[Test6].decode(ImmutableRecord(schema, Vector(new Utf8("foo"))), schema, DefaultFieldMapper) shouldBe Test6(Left("foo"))
    Decoder[Test6].decode(ImmutableRecord(schema, Vector(array)), schema, DefaultFieldMapper) shouldBe Test6(Right(Seq(234.4D, 235.5D)))
  }
}

