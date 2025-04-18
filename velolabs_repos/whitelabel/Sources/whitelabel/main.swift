
import Foundation

let path = Config.rootPath

let associations = Associations(apps: Config.apps)

do {
    if FileManager.default.fileExists(atPath: path) {
        try FileManager.default.removeItem(atPath: path)
    }
    let root = URL(fileURLWithPath: path)
    try FileManager.default.createDirectory(at: root, withIntermediateDirectories: false, attributes: nil)
    let data = try JSONEncoder().encode(associations).prettyJSON()
    try data.save(to: root, path: ".well-known", fileName: "apple-app-site-association")
    for app in Config.apps {
        try app.createLink(root: root)
    }
} catch {
    fatalError(error.localizedDescription)
}



