@groovy.transform.Field
String str = "this is a string"

def call(String name) {
    echo "Hello ${name}"
    echo str
}