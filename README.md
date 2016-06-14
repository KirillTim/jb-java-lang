# jb-java-lang

#### Что проверяется при создании классов, интерфейсов и методов
   Наследование классов и интерфейсов

   Проверка отсутсвия переопределения методов и коллизий возвращаемых значений

   Проверка модификаторов доступа для интерфейсов

   Проверка наличия реализации тела метода

   Проверка правильного использования `abstract`

   Нельзя наследоваться от `final` классов

   Нельзя перегружать `final` методы

   Нельзя делать new для абстактных классов

#### Реализованы [базовые классы для исключений и коллекций и некоторые литералы](https://github.com/KirillTim/jb-java-lang/blob/master/src/main/kotlin/im/kirillt/jbtask/builtin/Types.kt)

#### Что проверятеся в телах методов
   Соответсвие типов возращаемых из методов

   Соответсвие типов при создании переменной

   Проверка наличия методов и полей у объекта

   Области вдимости

   Кидать и ловить можно только подклассы `Throwable` и `CheckedException` соответсвенно

   Проверка повторного присваивания final переменной

   И много что ещё...

#### Тесты
Есть чуть-чуть :(

#### Проблемы
   Смотри `//TODO:`

