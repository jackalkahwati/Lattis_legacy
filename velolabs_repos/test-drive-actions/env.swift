import Foundation
import Dispatch

struct Parameter: Codable {
    let Name: String
    let Value: String
}

struct Response: Codable {
    let Parameters: [Parameter]
}


let sema = DispatchSemaphore(value: 0)

@discardableResult
func shell(_ args: String...) throws -> Response {
    let task = Process()
    let output = Pipe()
    if #available(macOS 10.13, *) {
        task.executableURL =  URL(string: "file:///usr/bin/env")
    } else {
        task.launchPath = "/usr/bin/env"
    }
    task.arguments = args
    task.standardOutput = output
    if #available(macOS 10.13, *) {
        try task.run()
    } else {
        task.launch()
    }
    task.waitUntilExit()
    let data = output.fileHandleForReading.readDataToEndOfFile()
    return try JSONDecoder().decode(Response.self, from: data)
}

func save(params: [Parameter]) throws {
    var string: String = "\n"
    for param in params {
        string.append(param.Name + "=" + param.Value + "\n")
    }
    try string.data(using: .utf8)?.write(to: URL(string: "file://./.env")!)
}

DispatchQueue.global().async {
    do {
        let response = try shell("aws", "ssm", "describe-parameters", "--parameter-filters", "Key=Name,Option=Contains,Values=/env/dev/circle")
        let names = response.Parameters.map(\.Name)
        let params = try shell("aws", "ssm", "get-parameters", "--names", names.joined(separator: " "), "--query", "Parameters[*].{Name:Name,Value:Value}")
        try save(params: params.Parameters)
        sema.signal()
    } catch {
        fatalError(error.localizedDescription)
    }
}

sema.wait()