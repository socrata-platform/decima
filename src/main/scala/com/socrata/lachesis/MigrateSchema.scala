//
//package com.socrata.lachesis
//
//import com.typesafe.config.ConfigFactory
//import com.socrata.lachesis.config.LachesisConfig
//import com.socrata.lachesis.db.Migration
//import com.socrata.lachesis.db.Migration.{Migrate, Undo, Redo}
//
//object MigrateSchema extends App {
//
//  override def main(args: Array[String]) {
//
//    val numChanges = args.length match {
//      case 2 => args(1).toInt
//      case 1 => 1
//      case _ =>
//        throw new IllegalArgumentException(
//          s"Incorrect number of arguments - expected 1 or 2 but received ${args.length}")
//    }
//
//    val operation = args(0).toLowerCase match {
//      case "migrate" => Migrate
//      case "undo" => Undo(numChanges)
//      case "redo" => Redo(numChanges)
//      case _ =>
//        throw new IllegalArgumentException(s"Unknown migration operation: '${args(0)}'")
//    }
//
//    val config = new LachesisConfig(ConfigFactory.load)
////    for {
////      conn <- )
////    } {
////      Migration.migrateDb(conn, operation)
////    }
//  }
//}