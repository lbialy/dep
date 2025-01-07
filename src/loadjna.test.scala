package ma.chinespirit.dep

import munit.FunSuite
import cue4s.{Prompts, InputProviderImpl}

class LoadJNATest extends FunSuite:

  test("loading jna for native-image agent") {
    Prompts.sync.use(_ => ())

    assert(true)
  }
