//
//  LockItem.swift
//  BLUF
//
//  Created by Ravil Khusainov on 22.08.2020.
//

import SwiftUI

struct LockItem: View {
    let lock: EllipseLock
    var body: some View {
        VStack {
            HStack {
                VStack(alignment: .leading) {
                    Text(lock.macId)
                        .font(.title2)
                    Text("Name: \(lock.name)")
//                    Text("Fleet: \(lock.metadata.fleet.name)")
                }
                Spacer()
            }
            Spacer()
        }
        .foregroundColor(.white)
        .padding()
        .background(LinearGradient(gradient: Gradient(colors: [.blue, .lightBlue]), startPoint: .top, endPoint: .bottom))
        .cornerRadius(10)
    }
}

struct LockItem_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            LockItem(lock: .init(metadata: .init(id: 0, macId: "DF456HUYUFF", name: "Test-1"), device: nil))
                .padding()
        }
            .previewDevice("iPhone 8")
            
    }
}
