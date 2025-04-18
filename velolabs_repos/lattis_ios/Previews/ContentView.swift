//
//  ContentView.swift
//  Previews
//
//  Created by Ravil Khusainov on 11.03.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import SwiftUI

struct ContentView: View {
    var body: some View {
        ZStack(alignment: .bottomLeading) {
            Rectangle()
                .fill(.green.opacity(0.4))
            HStack(alignment: .caerdTop) {
                VStack(alignment: .leading) {
                    Text("Some")
                    Text("Red")
                    Text("Some")
                    Text("Red")
                    Text("Some")
                    Text("Red")
                }
                Spacer()
            }
            .padding()
            .padding(.top)
            .background(
                Color.white
                    .cornerRadius(10)
                    .shadow(radius: 3)
            )
            .alignmentGuide(.caerdTop) { d in d[.top]}
//            .alignmentGuide(.bottom) { d in d[.bottom]}
            HStack {
                Text("Rented")
                Circle()
                    .fill(.white)
                    .frame(width: 4, height: 4)
                Text("5:33")
            }
            .foregroundColor(.white)
            .padding()
            .background(
                Color.black
            )
            .alignmentGuide(.caerdTop) { d in d[VerticalAlignment.center]}
        }
    }
}

extension VerticalAlignment {
    enum CardTopAlignment: AlignmentID {
        static func defaultValue(in context: ViewDimensions) -> CGFloat {
            context[.bottom]
        }
    }
    
    static let caerdTop = VerticalAlignment(CardTopAlignment.self)
}

extension Alignment {
    static let cardTop = Alignment(horizontal: .leading, vertical: .caerdTop)
}

struct EquipmentControlView: View {
    
    
    var body: some View {
        Button {
            
        } label: {
            HStack {
                Image(systemName: "lock")
                Text("unlock")
            }
            .padding(.horizontal)
            .frame(height: 44)
            .background(
                Capsule().fill(Color.black)
            )
            .foregroundColor(.red)
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        EquipmentControlView()
    }
}
