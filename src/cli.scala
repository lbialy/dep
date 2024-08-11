package ma.chinespirit.dep

import decline_derive.*
import com.monovore.decline.*

enum Dep derives CommandApplication:
  case Web

object Dep:
  def parse(args: String*): Either[Help, Dep] =
    CommandApplication.parse[Dep](args, sys.env)
