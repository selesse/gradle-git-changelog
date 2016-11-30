yieldUnescaped '<!DOCTYPE html>'
html {
    head {
        meta('charset':'UTF-8')
        title("$title")
    }

    body {
        h1("$title")
        h2("What a wonderful sample!")

        div {
            headingsToCommitMap.each { k, v ->
                if (k != "None") {
                    fragment "h2(title)", title: k
                }
                newLine()
                ul {
                    v.each {
                        fragment "li(commit)", commit: it
                        newLine()
                    }
                }
            }
        }
    }
}
