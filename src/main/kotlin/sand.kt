class sand {
}


fun main(args: Array<String>) {
    print("Hello World")
    val s: String = ""
    val bar: int = 40

    val ddd: String = if (bar < 42) {
        "abc"
    } else {
        "xyz"
    }

    val sankou = if (bar < 42) "abc" else "xyz"
    for (i in 0 until 100) {
        print(i)
    }

    for (i in 99 downTo 0) print(i)
    // for (int = 99; i >= 0; i--)

    for (i in 0 until 100 step 2) print(i)
    // for (int i = 0; i < 100; i += 2)

    for (i in 1..100) print(i)
    // for (int i = 1; i <= 100; i++)

    val xa = when (bar) {
        0 -> "abc"
        43 -> "def"
        1,2 -> "ddd"
        else -> "err"
    }
    val foo = Foo()
}

class Person {
    val firstName: String
    val lastName: String
    var age: Int



    constructor(
        firstName: String,
        lastName: String,
        age: Int
    ) {
        this.firstName = firstName
        this.lastName = lastName
        this.age = age
    }

    val fullName: String
        get() = firstName + " " + lastName

}

class Person2(val firstName: String, lastName: String) {
    var age: Int

    init {
        age = 0
    }

    fun elapse(years: Int = 1): Int {
        age += years
        return age
    }
}

open class NotFinalPerson(val firstName: String, val lastName: String, var age: Int) {
    open val fullName: String // final ではなくなる
        get() = firstName + " " + lastName
}

open class AbstractPerson(val firstName: String, val lastName, var age: Int) {
    open val fullName: String
        get() = firstName + " " + lastName
}

class EasternPerson(firstName: String, lastName: String, age: Int): AbstractPerson(firstName, lastName, age) {
    override val fullName: String
        get() = lastName + " " + firstName
}

var person = Person("first", "last", 12)
// person.age = person.age+1

interface InterfaceFoo {
    var bar : Int
    fun baz(qux: String)
}

class ExtendsInterfaceFoo(override val bar: Int): InterfaceFoo {
    override fun baz(qux: String) {
        print(qux)
    }
}