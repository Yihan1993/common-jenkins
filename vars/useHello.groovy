def useHelloFun(String str) {
    println(str)
    hello(str)
}

def runShellCheck() {
    def output = sh(script: "mkdir -p ./output/shellcheck",
                    returnStdout: true).trim()
    println (output)
}