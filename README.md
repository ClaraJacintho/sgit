#  👋 Welcome to sgit

This project is a clone of git in Scala, made for the Functional Programming class at Polytech Montpellier. 

The project started on October the 2nd and will end on October the 20th.
# ⚡  Installation
Clone the repository 

In the repository, run `sbt assembly`

The compiled JAR will be on the `target/<scala version>` directory (*Experimental:* alternatively, download the pre-compiled jar from the GitHub under “releases” ).

You can run it using `java -jar <path/to/jar> <command> <arg>` straight in the directory you want to build the sgit repo. You can also create an alias in your .bashrc file as: `alias sgit='java -jar '<path/to/jar>'`. You will then be able to run by typing `sgit <command> <arg>`.
# 📌 Commands 

 - [x] sgit init
 - [x] sgit status
 - [x] sgit diff
 - [x] sgit add \<filename>
 - [x] sgit commit
 - [x] sgit log
 - [ ] sgit log -p
 - [ ] sgit log -stat
 - [x] sgit branch  \<branch>
 - [x] sgit branch -av
 - [x] sgit checkout <branch\>
 - [x] sgit tag <tag\>
 - [ ] sgit merge <branch\>
 - [ ] sgit rebase <branch\>
 - [ ] sgit rebase -i
