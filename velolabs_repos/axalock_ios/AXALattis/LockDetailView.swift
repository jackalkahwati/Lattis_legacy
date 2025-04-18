//
//  LockDetailView.swift
//  AXALattis
//
//  Created by Ravil Khusainov on 30.03.2020.
//

import SwiftUI
import AXALock

struct LockDetailView: View {
    var body: some View {
        VStack {
            Wrap(AxaLockControl(lockedImage: UIImage(named: "icon_lock_secure"), unlockedImage: UIImage(named: "icon_lock_unsecure"))) { control in
                
            }
            .frame(width: 88, height: 48)
            Text("Some")
        }
    }
}


struct LockDetailView_Previews: PreviewProvider {
    static var previews: some View {
        LockDetailView()
    }
}
