//
//  ViewModifiers.swift
//  Operator
//
//  Created by Ravil Khusainov on 03.03.2021.
//

import SwiftUI

struct ViewStateModifier: ViewModifier {
    
    @Binding var state: ViewState
    let onAppear: () -> Void
    @State var opacity: Double = 0
    
    init(state: Binding<ViewState>, onAppear: @escaping () -> Void = { }) {
        self._state = state
        self.onAppear = onAppear
    }
    
    func body(content: Content) -> some View {
        ZStack {
            switch state {
            case .initial:
                content
                    .redacted(reason: .placeholder)
                    .disabled(true)
            case .loading:
                content
                    .redacted(reason: .placeholder)
                    .disabled(true)
                    .opacity(opacity)
                    .animation(Animation.easeInOut(duration: 1).repeatForever(autoreverses: true))
                    .onAppear {
                        opacity = 1
                    }
            case .error(let title, let text):
                content
                    .blur(radius: 4.0)
                ErrorView(title: title ?? "Warning", text: text ?? "It looks like there was an issue", state: $state)
            case .screen:
                content
            }
        }
        .animation(.easeInOut)
        .onAppear(perform: onAppear)
    }
}

enum ViewState {
    case initial
    case screen
    case loading
    case error(String?, String?)
}

struct AppStateModifier: ViewModifier {
    
    @StateObject var settings: UserSettings
    
    fileprivate var reason: RedactionReasons {
        switch settings.appState {
        case .main:
            return .init()
        default:
            return .placeholder
        }
    }
    
    fileprivate var radius: CGFloat {
        switch settings.appState {
        case .main, .splash, .signIn:
            return 0
        default:
            return 4
        }
    }
    
    func body(content: Content) -> some View {
        ZStack {
            content
                .animation(nil)
                .redacted(reason: reason)
                .blur(radius: radius)
            switch settings.appState {
            case .signIn:
                SignInView(logic: .init(settings))
            case .fleet:
                FleetsView(logic: .init(settings))
            case .splash:
                SplashView()
            default:
                EmptyView()
            }
        }
        .animation(.easeInOut)
    }
}

struct PopViewModifier: ViewModifier {
    func body(content: Content) -> some View {
        ZStack {
            Color.black
                .edgesIgnoringSafeArea(.all)
                .blur(radius: 300)
            content
                .frame(maxWidth: 500)
                .shadow(color: Color.primary.opacity(0.3), radius: 10)
                .transition(.move(edge: .bottom))
        }
    }
}

struct ScrollViewModifier: ViewModifier {
    
    func body(content: Content) -> some View {
        ScrollViewReader { proxy in
            content
        }
    }
}

struct PlaceholderViewModifier: ViewModifier {
    let isEmpty: Bool
    let placeholder: EmptyListView
    func body(content: Content) -> some View {
        if isEmpty {
            placeholder
        } else {
            content
        }
    }
}

struct ProgressIndicatorModifier: ViewModifier {
    let isActive: Bool
    func body(content: Content) -> some View {
        HStack {
            content
                .disabled(isActive)
            if isActive {
                Spacer()
                ProgressView()
            }
        }
    }
}

extension View {
    func viewState(_ state: Binding<ViewState>, onAppear: @escaping () -> Void = { }) -> some View {
        modifier(ViewStateModifier(state: state, onAppear: onAppear))
    }
    
    func appState(_ settings: UserSettings) -> some View {
        modifier(AppStateModifier(settings: settings))
    }
    
    func popView() -> some View {
        modifier(PopViewModifier())
    }
    
    func scrollView() -> some View {
        modifier(ScrollViewModifier())
    }
    
    func placeholder(_ isEmpty: Bool, view: EmptyListView) -> some View {
        modifier(PlaceholderViewModifier(isEmpty: isEmpty, placeholder: view))
    }
    
    func progress(indicator: Bool) -> some View {
        modifier(ProgressIndicatorModifier(isActive: indicator))
    }
}
