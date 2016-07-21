package org.phoenix.core.test

import org.scalatest._

abstract class UnitSpec extends FlatSpec with Matchers with
    OptionValues with Inside with Inspectors
