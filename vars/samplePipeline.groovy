def call(Map config = [:]) {
    pipeline {
        agent any
        stages {
            stage ('Hello') {
                steps {
                    hello("World")
                }
            }
            stage ('test var') {
                steps {
                    hello(config.var)
                }
            }
            stage ('use var in shell') {
                steps {
                    script {
                        println config.int
                    }
                }
            }
        }
    }
}