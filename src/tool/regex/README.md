# The Regular Expression Writing Rules in This Tool
## Warning
The writing rules of the regular expression in this tool is **different from the one in java.**
Please familiarize yourself with the rules before using the tool to avoid difficult syntax errors when using the tool
## Normal Rules
1. An ASCII character is a regex(regular expression)
2. **MN** is a regex if M and N are both regex. This rule refers to joining M and N
3. **M|N** is a regex if M and N are both regex. '|' means "or"
4. **M*** is a regex if M is a regex. This rule refers to joining 0 or more M together
5. **(M)** is a regex if M is a regex. The parentheses are just to raise the priority of the operation
## Syntactic Sugar
1. **.** is a regex that means choose one of the ASCII character
except '\n'
2. **M+** is a regex if M is a regex which refers to MM*
3. **\[C\]** is a regex if C is a valid charset, this means choose one of the 
character in the charset
## Charset Rules
1. start with the '^' means reverse charset
2. '-' refers to the range, the char before and after
it should both be digits, uppercase or lowercase. The char after it should
bigger than the char before it
## Special Character
The characters below either has special function or are the reserved character in regex, 
so if you want to use the original meaning of these characters,
you should add '\' before them to transfer the meaning

**You need to distinguish between regular expression escape characters and java escape characters**
### The special Character
\[\]()*+?:$^\-.