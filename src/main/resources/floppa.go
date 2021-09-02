package main

import (
	"fmt"
	"regexp"
	"strings"
)

func main() {
    input := `%CODE%`

	inputReaded := string(input)

	flopRegex := regexp.MustCompile(`fohoe\(([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\)|fofo\(([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\)|flop\(([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\)|floppa|caracal|keerr|keeerr|kee?r|foo?o?o?|go|no|flop|hoe|brb`)
	flopKeywords := flopRegex.FindAllString(inputReaded, -1)

	inputReferenceCount := 0

	compiledProgram := `package main
import "fmt"
func changeByte(mem *[30000]byte, c int, a int, t string) {
	if t == "+" {
		if int((*mem)[c])+a <= 256 {
			(*mem)[c] += byte(a)
		} else {
			(*mem)[c] = byte(a % 255)
		}
	} else {
		if int((*mem)[c])-a >= 0 {
			(*mem)[c] -= byte(a)
		} else {
			(*mem)[c] = byte(256 - (a % 255))
		}
	}
}
func main() {
    memory := [30000]byte{}
    current := 0
`

	programIndentation := 4

	for _, keyword := range flopKeywords {
		switch keyword {
		case "ker":
			compiledProgram += fmt.Sprintf("%schangeByte(&memory, current, 1, \"+\")\n", indentProgram(programIndentation))
		case "keer":
			compiledProgram += fmt.Sprintf("%schangeByte(&memory, current, 2, \"+\")\n", indentProgram(programIndentation))
		case "fo":
			compiledProgram += fmt.Sprintf("%schangeByte(&memory, current, 1, \"-\")\n", indentProgram(programIndentation))
		case "foo":
			compiledProgram += fmt.Sprintf("%schangeByte(&memory, current, 2, \"-\")\n", indentProgram(programIndentation))
		case "keerr":
			compiledProgram += fmt.Sprintf("%schangeByte(&memory, current, 3, \"+\")\n", indentProgram(programIndentation))
		case "keeerr":
			compiledProgram += fmt.Sprintf("%schangeByte(&memory, current, 4, \"+\")\n", indentProgram(programIndentation))
		case "fooo":
			compiledProgram += fmt.Sprintf("%schangeByte(&memory, current, 3, \"-\")\n", indentProgram(programIndentation))
		case "foooo":
			compiledProgram += fmt.Sprintf("%schangeByte(&memory, current, 4, \"-\")\n", indentProgram(programIndentation))
		case "go":
			compiledProgram += fmt.Sprintf("%scurrent ++\n", indentProgram(programIndentation))
		case "no":
			compiledProgram += fmt.Sprintf("%scurrent --\n", indentProgram(programIndentation))
		case "flop":
			compiledProgram += fmt.Sprintf("%sfor memory[current] != 0 {\n", indentProgram(programIndentation))
			programIndentation += 4
		case "hoe":
			programIndentation -= 4
			compiledProgram += fmt.Sprintf("%s}\n", indentProgram(programIndentation))
		case "brb":
			compiledProgram += fmt.Sprintf("%spanic(\"Program Finished.\")\n", indentProgram(programIndentation))
		case "caracal":
			if inputReferenceCount == 0 {
				compiledProgram = strings.ReplaceAll(compiledProgram, "import \"fmt\"", "import \"fmt\"\nimport \"os\"\nimport \"bufio\"")
				compiledProgram += fmt.Sprintf("%sreader := bufio.NewReader(os.Stdin)\n", indentProgram(programIndentation))
				compiledProgram += fmt.Sprintf("%sstr, _ := reader.ReadString('\\n')\n%smemory[current] = str[0]\n", indentProgram(programIndentation), indentProgram(programIndentation))
			} else {
				compiledProgram += fmt.Sprintf("%sstr, _ = reader.ReadString('\\n')\n%smemory[current] = str[0]\n", indentProgram(programIndentation), indentProgram(programIndentation))
			}
			inputReferenceCount++
		case "floppa":
			compiledProgram += fmt.Sprintf("%sfmt.Print(string(memory[current]))\n", indentProgram(programIndentation))
		default:
			if strings.HasPrefix(keyword, "flop(") && strings.HasSuffix(keyword, ")") {
				compiledProgram += fmt.Sprintf("%sif memory[current] == byte(%s) {\n", indentProgram(programIndentation), strings.TrimSuffix(string(keyword[5:]), ")"))
				programIndentation += 4
			} else if strings.HasPrefix(keyword, "fohoe(") && strings.HasSuffix(keyword, ")") {
				compiledProgram += fmt.Sprintf("%sif memory[current] >= byte(%s) {\n", indentProgram(programIndentation), strings.TrimSuffix(string(keyword[6:]), ")"))
				programIndentation += 4
			} else if strings.HasPrefix(keyword, "fofo(") && strings.HasSuffix(keyword, ")") {
				compiledProgram += fmt.Sprintf("%sif memory[current] <= byte(%s) {\n", indentProgram(programIndentation), strings.TrimSuffix(string(keyword[5:]), ")"))
				programIndentation += 4
			}
		}
	}
	compiledProgram += "}"

    fmt.Printf("CODE: \n")
    fmt.Printf(string([]byte(compiledProgram)))
}

func indentProgram(indentation int) string {
	return strings.Repeat(" ", indentation)
}