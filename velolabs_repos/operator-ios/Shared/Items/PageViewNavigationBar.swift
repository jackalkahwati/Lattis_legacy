//
//  PageViewNavigationBar.swift
//  Operator
//
//  Created by Ravil Khusainov on 09.03.2021.
//

import SwiftUI

struct PageViewNavigationBar: View {
    
    let titles: [String]
    @Binding var selected: String
    
    var body: some View {
        HStack {
            ForEach(titles) { title in
                Button(action: { selected = title }, label: {
                    Text(title)
                        .underline(selected == title)
                })
                .frame(maxWidth: .infinity)
            }
        }
        .accentColor(.primary)
        .font(.headline)
        .padding([.leading, .top, .trailing])
    }
}

//struct TabViewNavigation_Previews: PreviewProvider {
//    static var previews: some View {
//        TabViewNavigation()
//    }
//}
