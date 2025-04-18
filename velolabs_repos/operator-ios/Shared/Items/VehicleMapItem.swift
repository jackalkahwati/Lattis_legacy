//
//  VehicleMapItem.swift
//  Operator
//
//  Created by Ravil Khusainov on 05.05.2021.
//

import SwiftUI

struct VehicleMapItem: View {
    let vehicle: Vehicle
    @State var progress: Float = 20
    
    var body: some View {
        ZStack {
            Image(iconName)
                .resizable()
                .aspectRatio(contentMode: .fit)
                .padding(10)
            ProgressBar(progress: $progress)
                .padding(2)
            if vehicle.metadata.batteryLevel != nil {
                VStack {
                    Image(systemName: "bolt.fill")
                        .font(.caption)
//                        .padding(.top, -8)
                        .foregroundColor(.white)
                    Spacer()
                }
            }
        }
        .frame(width: 54, height: 54)
        .background(Circle().fill(Color.accentColor))
        .ignoresSafeArea()
        .onAppear(perform: updateProgress)
        .ignoresSafeArea()
//        .padding(.top, -8)
    }
    
    var iconName: String {
        switch vehicle.metadata.group.type {
        case .cart:
            return "annotation_cart"
        case .electric, .regular:
            return "annotation_bike_regular"
        case .kickScooter:
            return "annotation_bike_kick_scooter"
        case .locker:
            return "annotation_locker"
        case .kayak:
            return "annotation_kayak"
        case .unknown:
             return ""
        }
    }
    
    func updateProgress() {
        progress = Float(vehicle.metadata.batteryLevel ?? 0)/100
    }
}

struct MapClusterView: View {
    let count: Int
    var body: some View {
        Text("\(count)")
            .foregroundColor(.white)
            .font(.title2)
            .padding(.vertical, 8)
            .padding(.horizontal, 16)
            .background(
                Capsule()
                    .strokeBorder(Color.white)
                    .background(Capsule().fill(Color.accentColor))
            )
            .ignoresSafeArea()
    }
}

struct VehicleMapItem_Previews: PreviewProvider {
    static var previews: some View {
        VStack {
            VehicleMapItem(vehicle: [Vehicle].dummy.first!)
            MapClusterView(count: 12)
        }
        .background(Color.black)
    }
}


struct ProgressBar: View {
    @Binding var progress: Float
    
    var body: some View {
        ZStack {
            Circle()
                .stroke(lineWidth: 2.0)
                .opacity(0.5)
                .foregroundColor(Color.white)
            
            Circle()
                .trim(from: 0.0, to: CGFloat(min(self.progress, 1.0)))
                .stroke(style: StrokeStyle(lineWidth: 2.0, lineCap: .round, lineJoin: .round))
                .foregroundColor(Color.white)
                .rotationEffect(Angle(degrees: 270.0))
        }
    }
}
