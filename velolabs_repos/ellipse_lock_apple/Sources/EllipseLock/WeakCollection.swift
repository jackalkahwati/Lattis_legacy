//
//  WeakCollection.swift
//  LattisSDK
//
//  Created by Ravil Khusainov on 8/1/18.
//  Copyright © 2018 Lattis Inc. All rights reserved.
//

import Foundation

class WeakCollection<A> {
    var storage = NSHashTable<AnyObject>.weakObjects()
    init(objects: [AnyObject] = []) {
        objects.forEach{storage.add($0)}
    }
}


extension WeakCollection: Collection {
    func index(after i: Int) -> Int {
        return storage.allObjects.index(after: i)
    }
    
    typealias Element = A
    typealias Index = Int
    
    var startIndex: WeakCollection<A>.Index {
        return storage.allObjects.startIndex
    }
    
    var endIndex: WeakCollection<A>.Index {
        return storage.allObjects.endIndex
    }
    
    subscript(index: Index) -> A {
        get {
            return storage.allObjects[index] as! A
        }
    }
    
    func insert(_ item: A) {
        let objsect = item as AnyObject
        guard !storage.contains(objsect) else { return }
        storage.add(item as AnyObject)
    }
    
    func delete(_ item: A) {
        storage.remove(item as AnyObject)
    }
}
