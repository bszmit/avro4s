package com.sksamuel.avro4s

import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.avro.specific.SpecificRecord

/**
  * An implementation of org.apache.avro.generic.GenericContainer that is both a
  * GenericRecord and a SpecificRecord.
  */
trait Record extends GenericRecord with SpecificRecord

case class ImmutableRecord(schema: Schema, values: Vector[AnyRef]) extends Record {
  require(schema.getType == Schema.Type.RECORD, "Cannot create an ImmutableRecord with a schema that is not a RECORD")

  import scala.collection.JavaConverters._

  override def put(key: String, v: scala.Any): Unit = throw new UnsupportedOperationException("This implementation of Record is immutable")
  override def put(i: Int, v: scala.Any): Unit = throw new UnsupportedOperationException("This implementation of Record is immutable")

  override def get(key: String): AnyRef = {
    val index = schema.getFields.asScala.indexWhere(_.name == key)
    if (index == -1)
      sys.error(s"Field $key does not exist in this record (schema=$schema, values=$values)")
    get(index)
  }

  override def get(i: Int): AnyRef = values(i)
  override def getSchema: Schema = schema
}
