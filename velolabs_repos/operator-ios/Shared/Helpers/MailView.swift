//
//  MailView.swift
//  Operator
//
//  Created by Ravil Khusainov on 22.06.2021.
//

import SwiftUI
import MessageUI


struct MailView: UIViewControllerRepresentable {
    static var canSendEmail: Bool { MFMailComposeViewController.canSendMail() }
    let recepients: [String]
    
    func makeUIViewController(context: Context) -> some UIViewController {
        let mail = MFMailComposeViewController()
        mail.mailComposeDelegate = context.coordinator
        mail.setToRecipients(recepients)
        mail.navigationBar.tintColor = UIColor(.primary)
        return mail
    }
    
    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {
        
    }
    
    func makeCoordinator() -> Coordinator {
        .init()
    }
    
    class Coordinator: NSObject, MFMailComposeViewControllerDelegate {
        func mailComposeController(_ controller: MFMailComposeViewController, didFinishWith result: MFMailComposeResult, error: Error?) {
            controller.dismiss(animated: true)
        }
    }
}

struct MessageView: UIViewControllerRepresentable {
    
    static var canSendMessage: Bool { MFMessageComposeViewController.canSendText() }
    let recepients: [String]
    
    func makeUIViewController(context: Context) -> some UIViewController {
        let message = MFMessageComposeViewController()
        message.messageComposeDelegate = context.coordinator
        message.recipients = recepients
        return message
    }
    
    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {
        
    }
    
    func makeCoordinator() -> Coordinator {
        .init()
    }
    
    final class Coordinator: NSObject, MFMessageComposeViewControllerDelegate {
        func messageComposeViewController(_ controller: MFMessageComposeViewController, didFinishWith result: MessageComposeResult) {
            controller.dismiss(animated: true)
        }
    }
}
