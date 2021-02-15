def useHelloFun(String str) {
    println(str)
    hello(str)
}

def runShellCheck() {
    sh (script: "touch test.txt")
    def output = sh(script: "find ./ -type f -name '*.txt'",
                    returnStdout: true).trim()
    println (output)
}