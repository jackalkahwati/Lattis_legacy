//
//  ThingListItem.swift
//  Operator
//
//  Created by Ravil Khusainov on 01.03.2021.
//

import SwiftUI

struct ThingListItem: View {
    let thing: Thing
    var body: some View {
        HStack {
            VStack(alignment: .leading) {
                Text(thing.metadata.vendor)
            }
            Spacer()
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color.accentColor.opacity(0.5))
        .cornerRadius(3.0)
    }
}

struct ThingListItem_Previews: PreviewProvider {
    static var previews: some View {
        ThingListItem(thing: [Thing].dummy.last!)
    }
}
