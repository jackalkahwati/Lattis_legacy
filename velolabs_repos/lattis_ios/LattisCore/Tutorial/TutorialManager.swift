//
//  TutorialManager.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 14.01.2021.
//  Copyright © 2021 Lattis inc. All rights reserved.
//

import UIKit
import Wrappers

public final class TutorialManager {
    public static let shared = TutorialManager()
    
    fileprivate(set) var files: [String] = []
    
    @UserDefaultsBacked(key: "tutorialsPresented", defaultValue: false)
    var presented: Bool
    
    var shouldPresent: Bool { !files.isEmpty && !presented }
    
    public func fill(map: (String) -> String?) {
        for idx in 1...10 {
            guard let path = map("tutorial_\(idx)"),
                  FileManager.default.fileExists(atPath: path) else { continue }
            files.append(path)
        }
    }
    
    func present(from: UIViewController, compleiton: @escaping () -> Void) {
        let tutorial = TutorialViewController(compleiton)
        tutorial.modalTransitionStyle = .crossDissolve
        tutorial.modalPresentationStyle = .overFullScreen
        presented = true
        from.present(tutorial, animated: true)
    }
    
    func controller(_ compleiton: @escaping () -> Void) -> TutorialViewController {
        let tutorial = TutorialViewController(compleiton)
        tutorial.modalTransitionStyle = .crossDissolve
        tutorial.modalPresentationStyle = .overFullScreen
        presented = true
        return tutorial
    }
}
