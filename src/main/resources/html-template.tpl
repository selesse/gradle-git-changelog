yieldUnescaped '<!DOCTYPE html>'
html {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
        title("$title")
    }

    body {
        h1("$title")

        div {
            headingsToCommitMap.each { k, v ->
                if (k != "None") {
                    fragment "h2(title)", title: k
                }
                v.each {
                    fragment "li(commit)", commit: it
                }
            }
        }
    }
}
