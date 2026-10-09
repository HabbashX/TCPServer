import static java.lang.IO.println;
import static java.lang.IO.readln;

void main() {

    println("please enter username");
    String username = readln();


    println("please enter age");
    int age = readInteger();


    println("please enter email");
    String email = readln();


    println("username: " + username);
    println("email: " + email);
    println("age: " + age);

}

int readInteger() {
    return Integer.parseInt(readln());
}
