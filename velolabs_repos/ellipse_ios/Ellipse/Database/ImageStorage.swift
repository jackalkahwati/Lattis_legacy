//
//  ImageStorage.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/10/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Oval

extension User {
    static var profilePhotoKey: String {
        return "ProfilePhoto"
    }
    
    func save(photo: UIImage?) {
        
        let filemanager = FileManager.default
        let path = filemanager.userDirectoryUrl(for: userId).appendingPathComponent(User.profilePhotoKey)
        if let p = photo {
        do {
            try p.jpegData(compressionQuality: 1.0)?.write(to: path)
        } catch {
            print(error)
            }
        } else {
            _ = try? filemanager.removeItem(at: path)
        }
    }
    
    func getPhoto(completion: @escaping (UIImage?) -> ()) {
        let filemanager = FileManager.default
        let path = filemanager.userDirectoryUrl(for: userId).appendingPathComponent(User.profilePhotoKey)
        do {
            let data = try Data(contentsOf: path)
            if let image = UIImage(data: data) {
                completion(image)
            } else {
                completion(nil)
            }
        } catch {
            print(error)
            if let string = pictureUrl, let url = URL(string: string) {
                Session.shared.download(by: url) { result in
                    switch result {
                    case .success(let data):
                        if let image = UIImage(data: data) {
                            completion(image)
                            self.save(photo: image)
                        } else {
                            completion(nil)
                        }
                    case .failure:
                        completion(nil)
                    }
                }
            } else {
                completion(nil)
            }
        }
    }
}
