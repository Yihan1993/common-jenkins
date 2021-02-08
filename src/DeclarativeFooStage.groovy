class DeclarativeFooStage {
    private final Script script

    DeclarativeFooStage(Script script) {
        this.script = script
    }

    // You can pass as many parameters as needed
    void execute(String name, boolean param1) {
        script.echo "Triggering script with name == ${name}"
        script.sh "echo 'Execute your desired bash command here'"

        if (param1) {
            script.sh "echo 'Executing conditional command, because param1 == true'"
        }
    }
}