//
//  ButtonStyles.swift
//  Operator
//
//  Created by Ravil Khusainov on 26.02.2021.
//

import SwiftUI

struct CreateButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        CreateButtonStyleView(configuration: configuration)
    }
}


private extension CreateButtonStyle {
    struct CreateButtonStyleView: View {
        
        @Environment(\.isEnabled) var isEnabled
        let configuration: CreateButtonStyle.Configuration
        
        var body: some View {
            configuration.label
                .font(.headline)
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding()
                .background(Color.accentColor.opacity(isEnabled ? 1 : 0.5))
                .cornerRadius(10)
        }
    }
}

struct SelectButtonStyle: ButtonStyle {
    let color: Color
    
    init(color: Color = Color.secondary.opacity(0.3)) {
        self.color = color
    }
    
    func makeBody(configuration: Configuration) -> some View {
        SelectButtonStyleView(configuration: configuration, color: color)
    }
}

private extension SelectButtonStyle {
    
    struct SelectButtonStyleView: View {
        
        let configuration: CreateButtonStyle.Configuration
        let color: Color
        
        var body: some View {
            configuration.label
                .foregroundColor(.white)
                .padding(5)
                .background(color)
                .cornerRadius(3.0)
        }
    }
}

struct CreateMenuStyle: MenuStyle {
    func makeBody(configuration: Configuration) -> some View {
        Menu(configuration)
            .font(.headline)
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding()
            .background(Color.accentColor)
            .cornerRadius(10)
    }
}

struct Style_Previews2: PreviewProvider {
    @State static var isOn = false
    @State static var processing = false
    static var previews: some View {
        Form {
            Button(action: {isOn.toggle()}, label: {
                Text("Button")
            })
            .buttonStyle(CreateButtonStyle())
            PrettyToggleButton(
                action: {isOn.toggle()},
                title: "Security",
                offImage: "lock.open",
                onImage: "lock",
                isOn: isOn,
                processing: processing
            )
        }
    }
}
