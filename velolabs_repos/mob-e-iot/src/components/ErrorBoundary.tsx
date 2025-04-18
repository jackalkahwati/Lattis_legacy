import React, { Component, ErrorInfo, ReactNode } from 'react';
import logger from '../utils/logger';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
}

interface State {
  hasError: boolean;
  error?: Error;
  errorInfo?: ErrorInfo;
}

class ErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
    error: undefined,
    errorInfo: undefined
  };

  public static getDerivedStateFromError(error: Error): State {
    return {
      hasError: true,
      error
    };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    this.setState({
      error,
      errorInfo
    });

    // Log error to our logging service
    logger.error('React Component Error', {
      error: {
        name: error.name,
        message: error.message,
        stack: error.stack
      },
      componentStack: errorInfo.componentStack,
      timestamp: new Date().toISOString()
    });
  }

  private handleReset = () => {
    this.setState({
      hasError: false,
      error: undefined,
      errorInfo: undefined
    });
  };

  public render() {
    if (this.state.hasError) {
      // Custom fallback UI
      return this.props.fallback || (
        <div className="flex items-center justify-center min-h-screen bg-background">
          <div className="p-6 rounded-lg shadow-lg bg-card max-w-lg w-full mx-4">
            <h2 className="text-2xl font-bold text-foreground mb-4">
              Something went wrong
            </h2>
            {process.env.NODE_ENV !== 'production' && this.state.error && (
              <div className="mb-4">
                <p className="text-red-500 mb-2">{this.state.error.message}</p>
                <pre className="bg-gray-100 p-2 rounded text-sm overflow-auto">
                  {this.state.error.stack}
                </pre>
              </div>
            )}
            <div className="flex gap-4">
              <button
                className="btn-primary px-4 py-2 rounded"
                onClick={this.handleReset}
              >
                Try again
              </button>
              <button
                className="btn-secondary px-4 py-2 rounded"
                onClick={() => window.location.reload()}
              >
                Reload page
              </button>
            </div>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
