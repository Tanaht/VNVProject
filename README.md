VNVProject
--

A project about Dynamic Analysis in the context of Validations & Verifications lessons

To run this tools first of all generate a jar file with the command: mvn package.

Take the jar-with-dependencies generated in the target folder and paste it onto the maven project who needs to be analysed.
 
And execute the command on the root of maven project to be analyzed:

```java -jar dynamicanalyser-1.0-SNAPSHOT-jar-with-dependencies.jar ./```

Of course you can use system property to extend normal behavior of this tool, see "Project Parameters" below.

The dynamic analyser program take the path to maven project in parameter so we need to add "./" in the command.


The folder 'input' in this project is not usefull at all it's just a little project used to test our dynamic analyser.

### Project Requirements
This project use mvn command so it is require to have mvn installed and correctly set in $PATH.

### Project Parameters


#### There is 3 System properties this application used:

instrumentation.execution_trace: true|false tells VNVProject to generate a Trace Execution report. (default set to false).

instrumentation.branch_coverage: true|false tells VNVProject to generate a Branch Coverage report. (default set to true).

log.redirect_output: true|false tells VNVProject to redirect inputs project outputs into $(input_project.home)/target/vnv/out.txt and the same for errput into err.txt, (default set to true). only available while "INFO" logging level is set

if instrumentation.execution_trace is set to true there is a last system property:

instrumentation.execution_trace.depth: [0..9]+|max tells VNVProject to generate a Trace execution report 
but for each test run it will record only a number of trace execution equals to this number, 
if it is set to 'max', then it will record all traces execution possible. (Default set to 3)
#### How to use them ?

These properties can be set when starting the program like this:

Replace $(input_project.home) with relative or absolute path to input project.
```sh
java -Dinstrumentation.execution_trace=true -Dlog.redirect_output=false -jar vnvproject.jar $(input_Project.home)
```

### Branch Coverage/Line Coverage:

The branch coverage report is presented in a text file at the following URL: 
$(input_Project.home)/target/vnv/vnv-analysis/VNVReport-BranchCoverage.txt

The report is presented with a line for each classes that has been reported:
    ``` ###org.apache.commons.cli.PatternOptionBuilder: ```
And a line for each method of each classes:
    ``` ###isValueCode(C)Z: ```
To avoid some issue Of the following: Same Unique Id Error, we write the signature of the method after the method name.

Finally on each method, the report wrote severall lines for each source code instruction lines:
```
Line: 130
	Counter 0: 50
	Counter 1: 46
	Counter 2: 45
	Counter 3: 39
	Counter 4: 36
	Counter 5: 35
	Counter 6: 33
	Counter 7: 32
	Counter 8: 31
	Counter 9: 28
	Counter 10: 23
	Counter 11: 27
	Counter 12: 50
All Branch Covered at least one time
```
For each line the BranchCoverage report place a counter on each blocks of the line (a block is a part of the code that is executed without any jump instruction)

So the source code related to this example from commons-cli project looks like it:
```java
public class PatternOptionBuilder
{
/*[...]*/    
/*128:*/    public static boolean isValueCode(final char ch)
/*129:*/    {
/*130:*/        return ch == '@'
/*131:*/                || ch == ':'
/*132:*/                || ch == '%'
/*133:*/                || ch == '+'
/*134:*/                || ch == '#'
/*135:*/                || ch == '<'
/*136:*/                || ch == '>'
/*137:*/                || ch == '*'
/*138:*/                || ch == '/'
/*139:*/                || ch == '!';
/*140:*/    }
/*[...]*/
}
```

SO the 130 line correspond to a chained list of 9 OR instruction. So there is 10 blocks betweens the OR instructions. 

But the report say there is 12 blocks.

Infact in this 130th line, there is one block before the start of the OR instructions, the 10 blocks of the OR instructions and one last block that is composed of the return instruction.
So there is effectively 12 blocks, and this is why the first block and the last one are executed the same time: 50 time. because they are always on each isValueCode() method call.

### Execution Trace:
On this project the execution trace instrumentation is the less stable instrumentation.

First of all we recommand to not perform trace execution on project with a lot of unit test: (More than 2000)
Because it will generate literally Gigabyte of trace execution text files. And it will text a lot of time to complete.
The trace execution files are stored like this: $(input_Project.home)/target/vnv/vnv-analysis/VNVReport-TraceExecution.$(number).txt
The number parameter correspond to a counter used to differentiate trace execution textfile: the generator split the trace execution files into small ones to be more easier to read on it.

Common bugs are 
- OutOfMemoryError (even if it seems to be fixed, it appears less frequently)
- Endless Loop (Unable to know why it is looping)


### Sucessfull run of DynamicAnalyser:

- For now only commons-cli input project seems to works correctly with trace execution.
- For Commons-collection it runs very correctly with a depth of 3 in execution trace: ```~/project/commons-collections $ java -Dinstrumentation.execution_trace=true -jar dynamicanalyser-1.0-SNAPSHOT-jar-with-dependencies.jar ./```
- For commons-lang it runs without major problems with the same settings of commons-collection.
