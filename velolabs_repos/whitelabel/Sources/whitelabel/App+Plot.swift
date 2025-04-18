//
//  App+Plot.swift
//  
//
//  Created by Ravil Khusainov on 15.06.2021.
//

import Foundation
import Plot

extension App {
    func createLink(root: URL) throws {
        guard let path = path, let link = link else { return }
        let html = HTML(
            .head(
                .meta(
                    .attribute(named: "http-equiv", value: "refresh"),
                    .content("0; url=\(link)")
                )
            )
        )
        let data = html.render(indentedBy: .tabs(1)).data(using: .utf8)
        try data?.save(to: root, path: path, fileName: "ios.html")
    }
}
