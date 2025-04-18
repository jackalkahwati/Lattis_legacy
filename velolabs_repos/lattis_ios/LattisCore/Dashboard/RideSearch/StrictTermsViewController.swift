//
//  StrictTermsViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 14.09.2021.
//  Copyright © 2021 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import Atributika

class StrictTermsViewController: UIViewController {

    fileprivate let contentView = UIStackView()
    fileprivate let actionControl = ActionContainer(left: .cancel)
    fileprivate let containerView = UIView()
    fileprivate let textView = UITextView()
    fileprivate let accept: () -> Void
    fileprivate let points: [String]
    fileprivate let logo: UIImage?
    fileprivate var acceptance: [Bool] = []
    
    init(_ points: [String], logo: UIImage? = nil, _ accept: @escaping () -> Void) {
        self.points = points
        self.accept = accept
        self.logo = .named("logo_welcome_grin")
        super.init(nibName: nil, bundle: nil)
        
        modalPresentationStyle = .overCurrentContext
        modalTransitionStyle = .crossDissolve
        
        actionControl.update(
            left: cancelAction(),
            right: acceptAction(enabled: false),
            priority: .right)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }
        
        view.backgroundColor = UIColor(white: 0.3, alpha: 0.8)
        view.addSubview(containerView)
        containerView.backgroundColor = .white
        containerView.layer.cornerRadius = .containerCornerRadius
        containerView.addShadow()
        
        containerView.addSubview(contentView)
        
        contentView.axis = .vertical
        contentView.spacing = .margin
        contentView.distribution = .equalCentering
        
        constrain(containerView, contentView, view) { container, stack, view in
            container.top == view.safeAreaLayoutGuide.top + .margin
            container.leading == view.leading + .margin
            container.bottom == view.safeAreaLayoutGuide.bottom - .margin
            container.trailing == view.trailing - .margin
            stack.edges == container.edges.inseted(by: .margin)
        }
        
        
        textView.isEditable = false
        if let image = logo {
            let imageView = UIImageView(image: image)
            imageView.contentMode = .center
            contentView.addArrangedSubview(imageView)
        }
        for text in points {
            contentView.addArrangedSubview(checkbox(text, index: acceptance.count))
            acceptance.append(false)
        }
        contentView.addArrangedSubview(AttributedLabel.legal(self, text: "Puedes consultar informacion adicional y detallada sobre Proteccion de Datos en la <a href=\"\(UITheme.theme.privacyPolicy)\">Politicia de Privacidad</a> y en los <a href=\"\(UITheme.theme.termsOfService)\">Terminos y Condiciones</a>."))
        contentView.addArrangedSubview(actionControl)
    }

    fileprivate func checkbox(_ text: String, index: Int) -> UIView {
        let container = UIView()
        let textLabel = UILabel.label(text: text, font: .theme(weight: .book, size: .text), lines: 0)
        container.addSubview(textLabel)
        let consentControl = UISwitch()
        consentControl.tag = index
        consentControl.onTintColor = UITheme.theme.color.accent
        container.addSubview(consentControl)
        consentControl.addTarget(self, action: #selector(consent(control:)), for: .valueChanged)
        constrain(container, textLabel, consentControl) { con, text, control in
            control.centerY == con.centerY
            control.leading == con.leading
            control.width == 44
            
            text.leading == control.trailing + .margin
            text.trailing == con.trailing
            text.top == con.top
            text.bottom == con.bottom
        }
        return container
    }
    
    @objc
    fileprivate func consent(control: UISwitch) {
        acceptance[control.tag] = control.isOn
        actionControl.update(left: cancelAction(), right: acceptAction(enabled: !acceptance.contains(false)), priority: .right)
    }
    
    fileprivate func cancelAction() -> ActionButton.Action {
        .plain(title: "cancel".localized(), style: .plain) { self.dismiss(animated: true)}
    }
    
    fileprivate func acceptAction(enabled: Bool) -> ActionButton.Action {
        if enabled {
            return .plain(title: "Aceptar", handler: accept)
        }
        return .plain(title: "Aceptar")
    }
}
