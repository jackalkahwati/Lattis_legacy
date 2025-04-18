//
//  BatteryVew.swift
//  LeMond
//
//  Created by Ravil Khusainov on 01.01.2022.
//

import SwiftUI

struct BatteryVew: View {
    
    @State var level: Float
    
    var body: some View {
        HStack {
            ZStack {
                HStack {
                    HStack {
                        GeometryReader { reader in
                            Rectangle()
                                .fill(Color.green)
                                .frame(width: reader.frame(in: .global).width*CGFloat(level))
                        }
                    }
                    .opacity(0.7)
                    .cornerRadius(4)
                    .padding(4)
                }
                .frame(height: 30)
                .background(Color.black.opacity(0.1))
                .cornerRadius(8)
                Label("\(Int(level*100))%", systemImage: "minus.plus.batteryblock.fill")
                    .foregroundColor(.white)
                    .font(.footnote)
            }
        }
    }
}

struct BatteryVew_Previews: PreviewProvider {
    static var previews: some View {
        BatteryVew(level: 20)
    }
}
