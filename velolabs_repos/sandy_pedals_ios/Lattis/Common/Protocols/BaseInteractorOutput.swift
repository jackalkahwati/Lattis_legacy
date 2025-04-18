//
//  BaseInteractorOutput.swift
//  Lattis
//
//  Created by Ravil Khusainov on 16/03/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation

protocol BaseInteractorOutput: class {
    func startLoading(with title: String?)
    func stopLoading(completion:(() -> ())?)
    func show(error: Error, file: String, line: Int)
    func show(error: Error, file: String, line: Int, action: @escaping () -> ())
    func warning(with title: String, subtitle: String?)
    func warning(with title: String, subtitle: String?, action: @escaping () -> ())
}
